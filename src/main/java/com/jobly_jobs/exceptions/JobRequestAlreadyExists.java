package com.jobly_jobs.exceptions;

public class JobRequestAlreadyExists extends RuntimeException {

  public JobRequestAlreadyExists(String message) {
    super(message);
  }
}
