package com.jobly_jobs.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jobly_jobs.domain.dto.response.ErrorDto;
import com.jobly_jobs.exceptions.InvalidUrlException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

  @Test
  @DisplayName("Should return 500 when handling data access exception")
  void givenDataAccessException_whenHandleUnspecifiedExceptions_thenReturns500() {
    // given
    DataAccessException dataAccessException = new DataAccessException("Data access exception") {
    };

    // when
    ResponseEntity<ErrorDto> responseEntity = globalExceptionHandler.handleUnspecifiedExceptions(
        dataAccessException);

    // then
    assertEquals(500, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    assertEquals("500", responseEntity.getBody().status());
    assertEquals("Internal Server Error", responseEntity.getBody().error());
    assertEquals("Data access exception", responseEntity.getBody().message());
    assertEquals("Something unexpected happened. Internal server error.", responseEntity.getBody().displayMessage());
  }

  @Test
  @DisplayName("Should return 400 when handling method argument not valid exception")
  void givenMethodArgumentNotValidException_whenHandleMethodArgumentNotValidException_thenReturns400() {
    // given
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("objectName", "fieldName", "error message");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException methodArgumentNotValidException = new MethodArgumentNotValidException(null,
        bindingResult);

    // when
    ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValidException(
        methodArgumentNotValidException);

    // then
    assertEquals(400, responseEntity.getStatusCode().value());
    assertNotNull(responseEntity.getBody());
    Map<String, String> body = (Map<String, String>) responseEntity.getBody();
    assertEquals("error message", body.get("fieldName"));
  }

  @Test
  @DisplayName("Should return 400 when handling invalid URL exception")
  void givenInvalidUrlException_whenHandleInvalidUrlException_thenReturns400() {
    // given
    InvalidUrlException invalidUrlException = new InvalidUrlException("invalid-url");
    // when
    ResponseEntity<ErrorDto> responseEntity = globalExceptionHandler.handleInvalidUrlException(
        invalidUrlException);
    // then
    assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCode().value());
    assertEquals("400", responseEntity.getBody().status());
    assertEquals("Bad Request", responseEntity.getBody().error());
    assertEquals("Invalid URLs: [invalid-url]", responseEntity.getBody().message());
    assertEquals("Invalid URL: your website url is malformed or is not reachable",
        responseEntity.getBody().displayMessage());
  }

  @Test
  @DisplayName("Should return 500 when handling generic exception")
  void givenGenericException_whenHandleUnspecifiedExceptions_thenReturns500() {
    // given
    Exception exception = new RuntimeException("Unexpected error");

    // when
    ResponseEntity<ErrorDto> responseEntity = globalExceptionHandler.handleUnspecifiedExceptions(
        exception);

    // then
    assertEquals(500, responseEntity.getStatusCode().value());
    assertEquals("500", responseEntity.getBody().status());
    assertEquals("Internal Server Error", responseEntity.getBody().error());
    assertEquals("Unexpected error", responseEntity.getBody().message());
    assertEquals("Something unexpected happened. Internal server error.", responseEntity.getBody().displayMessage());
  }

}