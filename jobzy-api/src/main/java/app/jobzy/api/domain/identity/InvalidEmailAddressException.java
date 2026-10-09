package app.jobzy.api.domain.identity;

import app.jobzy.api.shared.exception.BaseException;

/**
 * Thrown when an e-mail address cannot be used to register: it is malformed or it is on a public
 * mail domain. The message never contains the address itself, because it is personal data and ends
 * up in responses and logs.
 */
public class InvalidEmailAddressException extends BaseException {

  /**
   * @param message why the address cannot be used, without the address itself
   */
  public InvalidEmailAddressException(String message) {
    super(message);
  }
}
