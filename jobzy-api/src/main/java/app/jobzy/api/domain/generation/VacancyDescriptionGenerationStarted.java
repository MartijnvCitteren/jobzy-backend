package app.jobzy.api.domain.generation;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain event: a generation was saved as {@code PENDING} and is waiting for a draft. Carries only
 * the id; whoever handles it loads the generation itself.
 */
public record VacancyDescriptionGenerationStarted(UUID generationId) {

  public VacancyDescriptionGenerationStarted {
    Objects.requireNonNull(generationId, "generationId is required");
  }
}
