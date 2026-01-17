package app.jobzy.rest;

import app.jobzy.domain.dto.response.ErrorDto;
import app.jobzy.exceptions.BaseException;
import app.jobzy.exceptions.InvalidUrlException;
import app.jobzy.exceptions.VacancySessionExpired;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String INTERNAL_SERVER_ERROR_DISPLAY = "Something unexpected happened. Internal server error.";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    Map<String, String> exceptionMap = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
        ));
    return ResponseEntity.badRequest().body(exceptionMap);
  }

  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<ErrorDto> handleInvalidUrlException(InvalidUrlException e) {
    return ResponseEntity.status(e.getHttpStatus()).body(buildErrorDto(e));
  }

  @ExceptionHandler(VacancySessionExpired.class)
  public ResponseEntity<ErrorDto> handleVacancySessionExpired(VacancySessionExpired e) {
    return ResponseEntity.status(e.getHttpStatus()).body(buildErrorDto(e));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleUnspecifiedExceptions(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorDto.builder()
            .status(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message(e.getMessage())
            .displayMessage(INTERNAL_SERVER_ERROR_DISPLAY)
            .build());
  }

  private ErrorDto buildErrorDto(BaseException e) {
    return ErrorDto.builder()
        .status(String.valueOf(e.getHttpStatus().value()))
        .error(e.getHttpStatus().getReasonPhrase())
        .message(e.getInternalMessage())
        .displayMessage(e.getDisplayMessage())
        .build();
  }

}
