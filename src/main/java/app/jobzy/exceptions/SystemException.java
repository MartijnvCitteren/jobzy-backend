package app.jobzy.exceptions;

import org.springframework.http.HttpStatus;

public class SystemException extends BaseException {

  private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
  private static final String DISPLAY_MESSAGE = "Something unexpected happened, please try again.";

  public SystemException(String originalErrorMessage) {
    super(STATUS, originalErrorMessage, DISPLAY_MESSAGE);
  }
}
