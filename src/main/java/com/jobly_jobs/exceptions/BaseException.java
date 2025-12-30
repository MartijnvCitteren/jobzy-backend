package com.jobly_jobs.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
    private String displayMessage;

    public BaseException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public BaseException(HttpStatus httpStatus, String message, String displayMessage) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.displayMessage = displayMessage;
    }



}
