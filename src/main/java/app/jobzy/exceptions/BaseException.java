package app.jobzy.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

  private final HttpStatus httpStatus;
  private final String internalMessage;
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
