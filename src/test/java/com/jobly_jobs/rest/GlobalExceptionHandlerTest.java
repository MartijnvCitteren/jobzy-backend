package com.jobly_jobs.rest;

import com.jobly_jobs.domain.dto.response.ErrorDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void givenDataAccessException_whenHandleDataAccessException_thenReturns500() {
        // given
        DataAccessException dataAccessException = new DataAccessException("Data access exception") {
        };

        // when
        ResponseEntity<ErrorDto> responseEntity = globalExceptionHandler.handleDataAccessException(dataAccessException);

        // then
        assertEquals(500, responseEntity.getStatusCode().value());
    }

    @Test
    void givenMethodArgumentNotValidException_whenHandleMethodArgumentNotValidException_thenReturns400() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException methodArgumentNotValidException = new MethodArgumentNotValidException(null,
                                                                                                              bindingResult) {
        };

        // when
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValidException(
                methodArgumentNotValidException);

        // then
        assertEquals(400, responseEntity.getStatusCode().value());
    }

}