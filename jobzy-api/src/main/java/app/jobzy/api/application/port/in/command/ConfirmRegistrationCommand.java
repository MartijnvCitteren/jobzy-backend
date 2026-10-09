package app.jobzy.api.application.port.in.command;

import lombok.Builder;

@Builder
public record ConfirmRegistrationCommand(String token, String password) {

  /** Redacted: both the token and the password are secrets. */
  @Override
  public String toString() {
    return "ConfirmRegistrationCommand[redacted]";
  }
}
