package app.jobzy.api.shared.exception;

import java.io.Serial;
import java.util.Random;

public class BaseException extends RuntimeException {
  private static final String DEFAULT_LOG_CODE = "DEFAULT, please update log-code";
  private final String logCode;

  protected BaseException(String message) {
    super(message);
    logCode = DEFAULT_LOG_CODE;
  }

  protected BaseException(String logCode, String message){
    super(message);
    this.logCode = logCode;
  }
}
