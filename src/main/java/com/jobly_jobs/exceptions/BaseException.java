package com.jobly_jobs.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private HttpStatus httpStatus;
    private String internalMessage;
    private String displayMessage;

    protected BaseException(HttpStatus httpStatus, String internalMessage) {
        this.httpStatus = httpStatus;
        this.internalMessage = internalMessage;
    }

    protected BaseException(HttpStatus httpStatus, String internalMessage, String displayMessage) {
        this.httpStatus = httpStatus;
        this.internalMessage = internalMessage;
        this.displayMessage = displayMessage;
    }
}
