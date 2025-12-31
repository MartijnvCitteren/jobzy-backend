package com.jobly_jobs.rest;

import com.jobly_jobs.domain.dto.response.ErrorDto;
import com.jobly_jobs.exceptions.BaseException;
import com.jobly_jobs.exceptions.InvalidUrlException;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR_DISPLAY = "Something unexpected happend. Internal server error.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> exceptionMap = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(exceptionMap);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorDto> handleInvalidUrlException(InvalidUrlException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(buildErrorDto(e));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> handleDataAccessException(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, e, INTERNAL_SERVER_ERROR_DISPLAY));
    }




    private ErrorDto buildErrorDto(BaseException e) {
        return ErrorDto.builder()
                .status(String.valueOf(e.getHttpStatus().value()))
                .error(e.getHttpStatus().getReasonPhrase())
                .message(e.getMessage())
                .displayMessage(e.getDisplayMessage())
                .build();
    }

    private ErrorDto buildErrorDto(HttpStatus httpStatus, Exception e,  String displayMessage) {
        return ErrorDto.builder()
                .status(String.valueOf(httpStatus.value()))
                .error(httpStatus.getReasonPhrase())
                .message(e.getMessage())
                .displayMessage(displayMessage)
                .build();
    }


}
