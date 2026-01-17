package app.jobzy.exceptions;

import org.springframework.http.HttpStatus;

public class VacancySessionExpired extends BaseException {
  private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
  private static final String DISPLAY_MESSAGE = "Your session has expired, please complete your vacancy creation within 60 minutes.";

  public VacancySessionExpired(String internalMessage) {
    super(STATUS, internalMessage, DISPLAY_MESSAGE);
  }

  public VacancySessionExpired() {
    super(STATUS, DISPLAY_MESSAGE);
  }
}
