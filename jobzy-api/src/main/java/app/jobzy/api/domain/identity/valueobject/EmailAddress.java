package app.jobzy.api.domain.identity.valueobject;

import app.jobzy.api.domain.identity.InvalidEmailAddressException;
import java.util.Locale;

/**
 * A syntactically valid e-mail address, normalised to lower case so that the same mailbox always
 * maps to the same user and the same domain to the same company.
 *
 * <p>The check is deliberately shallow (one {@code @}, a local part, a domain with a dot): bean
 * validation already rejects malformed input at the edge, and only a mail that arrives proves an
 * address really works. What the domain needs is a reliable {@link #domain()}.
 */
public record EmailAddress(String value) {

  public EmailAddress {
    if (value == null) {
      throw new InvalidEmailAddressException("E-mail address is required");
    }
    value = value.trim().toLowerCase(Locale.ROOT);
    int at = value.indexOf('@');
    if (at <= 0
        || at != value.lastIndexOf('@')
        || value.chars().anyMatch(Character::isWhitespace)) {
      throw new InvalidEmailAddressException("E-mail address is not valid");
    }
    String domain = value.substring(at + 1);
    if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
      throw new InvalidEmailAddressException("E-mail address is not valid");
    }
  }

  /** The part after the {@code @}, in lower case. The key that links a user to a company. */
  public String domain() {
    return value.substring(value.indexOf('@') + 1);
  }

  /** Redacted: an e-mail address is personal data and must not end up in logs. */
  @Override
  public String toString() {
    return "EmailAddress[redacted]";
  }
}
