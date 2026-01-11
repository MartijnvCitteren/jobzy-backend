package com.jobly_jobs.exceptions;

import java.util.Arrays;
import org.springframework.http.HttpStatus;

public class InvalidUrlException extends BaseException {

  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String DISPLAY_MESSAGE = "Invalid URL: your website url is malformed or is not reachable";

  public InvalidUrlException(String... url) {
    super(STATUS, "Invalid URLs: " + Arrays.toString(url), DISPLAY_MESSAGE);
  }


}
