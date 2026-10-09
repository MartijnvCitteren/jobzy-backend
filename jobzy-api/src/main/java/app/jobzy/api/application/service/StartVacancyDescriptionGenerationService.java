package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.StartVacancyDescriptionGenerationUseCase;
import app.jobzy.api.application.port.in.command.StartVacancyDescriptionGenerationCommand;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.generation.VacancyDescriptionGeneration;
import app.jobzy.api.domain.generation.VacancyDescriptionGenerationStarted;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.vacancy.VacancyNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starts a description generation: snapshots the vacancy's job title and language together with the
 * user's answers, saves the generation as {@code PENDING} and publishes {@link
 * VacancyDescriptionGenerationStarted}. The draft itself is produced by {@code
 * VacancyDescriptionGenerationStartedListener} after this transaction commits, so the {@code
 * PENDING} row is visible to a poll before any work on it starts.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StartVacancyDescriptionGenerationService
    implements StartVacancyDescriptionGenerationUseCase {
  private final VacancyRepository vacancyRepository;
  private final VacancyDescriptionGenerationRepository generationRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  @Override
  @Transactional
  public VacancyDescriptionGeneration start(StartVacancyDescriptionGenerationCommand command) {
    var vacancy =
        vacancyRepository
            .findById(command.vacancyId())
            .orElseThrow(() -> new VacancyNotFoundException(command.vacancyId()));

    var input =
        new GenerationInput(
            vacancy.getJobTitle(),
            vacancy.getLanguage(),
            command.mostImportantTasks(),
            command.team(),
            command.whyNiceJob());
    var generation =
        VacancyDescriptionGeneration.start(vacancy.getId(), input, LocalDateTime.now(clock));

    generationRepository.save(generation);
    eventPublisher.publishEvent(new VacancyDescriptionGenerationStarted(generation.getId()));
    log.info("Description generation is started: {}", generation);

    return generation;
  }
}
