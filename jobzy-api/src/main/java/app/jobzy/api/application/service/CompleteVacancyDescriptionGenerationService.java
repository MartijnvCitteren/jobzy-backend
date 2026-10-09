package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.CompleteVacancyDescriptionGenerationUseCase;
import app.jobzy.api.application.port.out.GenerationResult;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerator;
import app.jobzy.api.domain.generation.VacancyDescriptionGeneration;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Produces the draft for a {@code PENDING} generation and records the outcome.
 *
 * <p>Runs from an {@code AFTER_COMMIT} listener. There the starting transaction is still bound to
 * the thread when the executor is synchronous (as in the integration tests), and a plain
 * transactional call would join that already committed transaction and its writes would never be
 * flushed. Both database steps therefore run in a new transaction ({@code REQUIRES_NEW}). They are
 * two short transactions around the provider call rather than one around the whole method, so no
 * database connection is held while waiting up to a minute on the AI provider.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CompleteVacancyDescriptionGenerationService
    implements CompleteVacancyDescriptionGenerationUseCase {
  private final VacancyDescriptionGenerationRepository generationRepository;
  private final VacancyDescriptionGenerator generator;
  private final Clock clock;
  private final PlatformTransactionManager transactionManager;

  @Override
  public void complete(UUID generationId) {
    Optional<VacancyDescriptionGeneration> pending = findPending(generationId);
    if (pending.isEmpty()) {
      log.warn("Generation {} is gone or already ended, nothing to complete", generationId);
      return;
    }
    var generation = pending.get();

    var result = generateSafely(generation.getInput());
    var now = LocalDateTime.now(clock);
    switch (result) {
      case GenerationResult.Drafted drafted ->
          generation.acceptDraft(drafted.draft(), drafted.model(), drafted.promptVersion(), now);
      case GenerationResult.Rejected rejected -> generation.fail(rejected.reason(), now);
    }

    newTransaction().executeWithoutResult(status -> generationRepository.save(generation));
    log.info("Description generation is completed: {}", generation);
  }

  private Optional<VacancyDescriptionGeneration> findPending(UUID generationId) {
    Optional<VacancyDescriptionGeneration> found =
        newTransaction().execute(status -> generationRepository.findById(generationId));
    return Optional.ofNullable(found)
        .flatMap(generation -> generation)
        .filter(generation -> generation.getStatus() == GenerationStatus.PENDING);
  }

  private TransactionTemplate newTransaction() {
    var template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return template;
  }

  /**
   * The port promises not to throw, but this runs on a background thread where an exception would
   * only be logged and leave the generation {@code PENDING} until it times out. Recording it as a
   * provider error gives the user a definite answer right away.
   */
  private GenerationResult generateSafely(GenerationInput input) {
    try {
      return generator.generate(input);
    } catch (RuntimeException e) {
      log.error("Description generator failed unexpectedly", e);
      return new GenerationResult.Rejected(GenerationFailureReason.PROVIDER_ERROR);
    }
  }
}
