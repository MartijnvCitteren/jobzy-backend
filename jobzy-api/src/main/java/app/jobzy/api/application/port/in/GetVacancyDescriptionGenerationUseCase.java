package app.jobzy.api.application.port.in;

import app.jobzy.api.domain.generation.VacancyDescriptionGeneration;
import java.util.UUID;

public interface GetVacancyDescriptionGenerationUseCase {

  /**
   * Returns the generation as a poll right now has to report it, with the 90-second timeout rule
   * applied.
   *
   * @throws app.jobzy.api.domain.generation.VacancyDescriptionGenerationNotFoundException if the
   *     generation does not exist for this vacancy
   */
  VacancyDescriptionGeneration get(UUID vacancyId, UUID generationId);
}
