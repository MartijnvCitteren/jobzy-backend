package app.jobzy.api.adapter.in.rest.generation;

import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.shared.exception.BaseException;

/**
 * Thrown by the controller when a polled generation has {@code FAILED}: the contract reports a
 * failed generation as a problem response, not as a {@code 200} with status {@code FAILED}.
 */
public class VacancyDescriptionGenerationFailedException extends BaseException {
  private final GenerationFailureReason reason;

  public VacancyDescriptionGenerationFailedException(GenerationFailureReason reason) {
    super("Description generation failed: " + reason);
    this.reason = reason;
  }

  public GenerationFailureReason getReason() {
    return reason;
  }
}
