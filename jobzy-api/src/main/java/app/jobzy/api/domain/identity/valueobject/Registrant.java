package app.jobzy.api.domain.identity.valueobject;

import java.util.Objects;

/** Who registers: the profile fields from the registration form. */
public record Registrant(String firstName, String lastName, EmailAddress email, JobRole jobRole) {

  public Registrant {
    firstName = requireNonBlank(firstName, "firstName");
    lastName = requireNonBlank(lastName, "lastName");
    Objects.requireNonNull(email, "email is required");
    Objects.requireNonNull(jobRole, "jobRole is required");
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value.trim();
  }

  /** Redacted: names and e-mail address are personal data and must not end up in logs. */
  @Override
  public String toString() {
    return "Registrant[jobRole=" + jobRole + "]";
  }
}
