package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.GetVacancyDescriptionGenerationUseCase;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.domain.generation.VacancyDescriptionGeneration;
import app.jobzy.api.domain.generation.VacancyDescriptionGenerationNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Answers a poll. A generation is only found through the vacancy it was started for, so a
 * generation id cannot be used to read a draft via another vacancy's URL.
 */
@Service
@RequiredArgsConstructor
public class GetVacancyDescriptionGenerationService
    implements GetVacancyDescriptionGenerationUseCase {
  private final VacancyDescriptionGenerationRepository generationRepository;
  private final Clock clock;

  @Override
  public VacancyDescriptionGeneration get(UUID vacancyId, UUID generationId) {
    return generationRepository
        .findById(generationId)
        .filter(generation -> generation.getVacancyId().equals(vacancyId))
        .map(generation -> generation.observedAt(LocalDateTime.now(clock)))
        .orElseThrow(
            () -> new VacancyDescriptionGenerationNotFoundException(vacancyId, generationId));
  }
}
