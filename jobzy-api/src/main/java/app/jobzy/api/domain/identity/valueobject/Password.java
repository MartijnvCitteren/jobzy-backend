package app.jobzy.api.domain.identity.valueobject;

import app.jobzy.api.domain.identity.InvalidPasswordException;
import java.util.Objects;

/**
 * A plain-text password that meets the password policy: 12 to 128 characters and no composition
 * rules, following NIST SP 800-63B (length helps, forced character classes do not). Characters are
 * counted as code points, so an emoji counts as one. Only lives long enough to be hashed.
 */
public final class Password {
  public static final int MIN_LENGTH = 12;
  public static final int MAX_LENGTH = 128;

  private final String value;

  public Password(String value) {
    Objects.requireNonNull(value, "password is required");
    int length = value.codePointCount(0, value.length());
    if (length < MIN_LENGTH || length > MAX_LENGTH) {
      throw new InvalidPasswordException(
          "Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }

  /** Redacted, so a password never ends up in a log line or exception message. */
  @Override
  public String toString() {
    return "Password[redacted]";
  }
}
