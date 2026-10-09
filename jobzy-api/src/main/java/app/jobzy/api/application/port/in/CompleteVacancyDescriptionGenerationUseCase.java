package app.jobzy.api.application.port.in;

import java.util.UUID;

public interface CompleteVacancyDescriptionGenerationUseCase {

  /**
   * Produces the draft for a {@code PENDING} generation and records the outcome. Does nothing for a
   * generation that no longer exists or already ended.
   */
  void complete(UUID generationId);
}
