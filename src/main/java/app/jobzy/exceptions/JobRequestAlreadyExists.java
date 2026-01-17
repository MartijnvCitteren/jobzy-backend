package app.jobzy.exceptions;

public class JobRequestAlreadyExists extends RuntimeException {

  public JobRequestAlreadyExists(String message) {
    super(message);
  }
}
