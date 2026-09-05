package app.jobzy.api.adapter.in.rest;

import app.jobzy.api.vacancy.adapter.in.web.contract.ProblemDetails;
import app.jobzy.api.vacancy.adapter.in.web.contract.ProblemDetailsErrorsInner;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions from every REST controller into the {@code ProblemDetails} representation
 * of the API contract.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String VALIDATION_FAILED_TITLE = "Validation failed";
  private static final String VALIDATION_FAILED_DETAIL =
      "The request failed validation on one or more fields, see 'errors' for details.";

  /**
   * Turns bean-validation failures on a request body into an RFC 9457 problem response. Every
   * violation is reported individually under {@code errors}, naming the offending field so the
   * client can point the user at the exact input that needs fixing. The rejected value itself is
   * deliberately not echoed back: this advice applies to every controller, so it would eventually
   * reflect (and log) personal data from candidate-facing payloads.
   *
   * <p>Returns a {@link ResponseEntity} rather than the body plus {@code @ResponseStatus}, so that
   * the {@code application/problem+json} content type the contract declares for error responses is
   * set explicitly instead of being left to content negotiation.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetails> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<ProblemDetailsErrorsInner> errors =
        ex.getBindingResult().getAllErrors().stream().map(this::toError).toList();

    var problemDetails =
        new ProblemDetails(VALIDATION_FAILED_TITLE, HttpStatus.BAD_REQUEST.value());
    problemDetails.setDetail(VALIDATION_FAILED_DETAIL);
    problemDetails.setInstance(URI.create(request.getRequestURI()));
    problemDetails.setErrors(errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetails);
  }

  private ProblemDetailsErrorsInner toError(ObjectError error) {
    if (error instanceof FieldError fieldError) {
      return new ProblemDetailsErrorsInner(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return new ProblemDetailsErrorsInner(error.getObjectName(), error.getDefaultMessage());
  }
}
