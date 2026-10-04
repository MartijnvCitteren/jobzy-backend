package app.jobzy.api.adapter.out.exception;

public class InvalidAiResponse extends RuntimeException {

  public InvalidAiResponse(String message) {
    super(message);
  }
}
