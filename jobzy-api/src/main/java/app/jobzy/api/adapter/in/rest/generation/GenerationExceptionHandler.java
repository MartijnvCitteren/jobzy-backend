package app.jobzy.api.adapter.in.rest.generation;

import app.jobzy.api.domain.generation.VacancyDescriptionGenerationNotFoundException;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.shared.Constants;
import app.jobzy.api.vacancy.adapter.in.web.contract.ProblemDetails;
import app.jobzy.api.vacancy.adapter.in.web.contract.ProblemDetailsErrorsInner;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates generation-specific exceptions into the {@code ProblemDetails} representation of the
 * API contract. Ordered at highest precedence for the same reason as {@code
 * VacancyExceptionHandler}: the catch-all in {@code GlobalExceptionHandler} must not shadow it.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GenerationExceptionHandler {

  @ExceptionHandler(VacancyDescriptionGenerationNotFoundException.class)
  ResponseEntity<ProblemDetails> handleGenerationNotFound(
      VacancyDescriptionGenerationNotFoundException ex, HttpServletRequest request) {
    return respond(
        HttpStatus.NOT_FOUND,
        problem(HttpStatus.NOT_FOUND, Constants.NOT_FOUND_TITLE, ex.getMessage(), request));
  }

  /**
   * A failed generation as the contract describes it: {@code 422} when the input was rejected,
   * because only changing the input helps; {@code 500} for every failure on our side, where
   * starting a new generation may help. The detail never repeats model output.
   */
  @ExceptionHandler(VacancyDescriptionGenerationFailedException.class)
  ResponseEntity<ProblemDetails> handleGenerationFailed(
      VacancyDescriptionGenerationFailedException ex, HttpServletRequest request) {
    var status =
        ex.getReason() == GenerationFailureReason.INPUT_REJECTED
            ? HttpStatus.UNPROCESSABLE_CONTENT
            : HttpStatus.INTERNAL_SERVER_ERROR;
    return respond(
        status,
        problem(status, Constants.GENERATION_FAILED_TITLE, detail(ex.getReason()), request));
  }

  /** Same shape as a bean-validation failure: the field is named, its value is not echoed. */
  @ExceptionHandler(InvalidGenerationRequestException.class)
  ResponseEntity<ProblemDetails> handleInvalidGenerationRequest(
      InvalidGenerationRequestException ex, HttpServletRequest request) {
    var problemDetails =
        problem(
            HttpStatus.BAD_REQUEST,
            Constants.VALIDATION_FAILED_TITLE,
            Constants.VALIDATION_FAILED_DETAIL,
            request);
    problemDetails.setErrors(
        List.of(new ProblemDetailsErrorsInner(ex.getField(), ex.getMessage())));
    return respond(HttpStatus.BAD_REQUEST, problemDetails);
  }

  private static String detail(GenerationFailureReason reason) {
    return switch (reason) {
      case INPUT_REJECTED ->
          "The answers were not recognised as a description of this job. Adjust them and start a"
              + " new generation.";
      case TIMEOUT -> "The generation did not finish in time. Start a new generation.";
      case PROVIDER_ERROR ->
          "The AI provider could not generate a description. Start a new generation.";
      case INVALID_OUTPUT -> "The generated text did not pass validation. Start a new generation.";
    };
  }

  private static ProblemDetails problem(
      HttpStatus status, String title, String detail, HttpServletRequest request) {
    var problemDetails = new ProblemDetails(title, status.value());
    problemDetails.setDetail(detail);
    problemDetails.setInstance(URI.create(request.getRequestURI()));
    return problemDetails;
  }

  private static ResponseEntity<ProblemDetails> respond(
      HttpStatus status, ProblemDetails problemDetails) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }
}
