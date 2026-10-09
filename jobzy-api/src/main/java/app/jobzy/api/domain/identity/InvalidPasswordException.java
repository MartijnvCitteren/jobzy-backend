package app.jobzy.api.domain.identity;

import app.jobzy.api.shared.exception.BaseException;

/** Thrown when a password does not meet the password policy. Never contains the password. */
public class InvalidPasswordException extends BaseException {

  /**
   * @param message which rule the password breaks
   */
  public InvalidPasswordException(String message) {
    super(message);
  }
}
