package app.jobzy.api.application.service;

import static app.jobzy.api.testSupport.GenerationFactory.MODEL;
import static app.jobzy.api.testSupport.GenerationFactory.PROMPT_VERSION;
import static app.jobzy.api.testSupport.GenerationFactory.REQUESTED_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.jobzy.api.application.port.out.GenerationResult;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerator;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.testSupport.GenerationFactory;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
class CompleteVacancyDescriptionGenerationServiceTest {

  private static final LocalDateTime NOW = REQUESTED_AT.plusSeconds(20);

  @Mock private VacancyDescriptionGenerationRepository generationRepository;
  @Mock private VacancyDescriptionGenerator generator;
  @Mock private PlatformTransactionManager transactionManager;

  private CompleteVacancyDescriptionGenerationService service;

  @BeforeEach
  void setUp() {
    var clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    service =
        new CompleteVacancyDescriptionGenerationService(
            generationRepository, generator, clock, transactionManager);
  }

  @Test
  @DisplayName(
      "given a pending generation and a draft from the generator, when complete then it is saved"
          + " as completed")
  void givenPendingGenerationAndDraftWhenCompleteThenSavedAsCompleted() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));
    when(generator.generate(generation.getInput()))
        .thenReturn(new GenerationResult.Drafted(GenerationFactory.draft(), MODEL, PROMPT_VERSION));

    service.complete(generation.getId());

    assertEquals(GenerationStatus.COMPLETED, generation.getStatus());
    assertEquals(GenerationFactory.draft(), generation.getDraft());
    assertEquals(NOW, generation.getCompletedAt());
    verify(generationRepository).save(generation);
  }

  @Test
  @DisplayName(
      "given a pending generation, when complete then load and save each run in a new"
          + " transaction")
  void givenPendingGenerationWhenCompleteThenLoadAndSaveRunInNewTransactions() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));
    when(generator.generate(any()))
        .thenReturn(new GenerationResult.Rejected(GenerationFailureReason.PROVIDER_ERROR));

    service.complete(generation.getId());

    verify(transactionManager, times(2))
        .getTransaction(
            argThat(
                (TransactionDefinition definition) ->
                    definition.getPropagationBehavior()
                        == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
  }

  @Test
  @DisplayName(
      "given a pending generation and a rejection, when complete then it is saved as failed with"
          + " that reason")
  void givenPendingGenerationAndRejectionWhenCompleteThenSavedAsFailed() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));
    when(generator.generate(generation.getInput()))
        .thenReturn(new GenerationResult.Rejected(GenerationFailureReason.INPUT_REJECTED));

    service.complete(generation.getId());

    assertEquals(GenerationStatus.FAILED, generation.getStatus());
    assertEquals(GenerationFailureReason.INPUT_REJECTED, generation.getFailureReason());
    verify(generationRepository).save(generation);
  }

  @Test
  @DisplayName(
      "given the generator throws, when complete then the generation is saved as a provider"
          + " error")
  void givenGeneratorThrowsWhenCompleteThenSavedAsProviderError() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));
    when(generator.generate(any())).thenThrow(new IllegalStateException("unexpected"));

    service.complete(generation.getId());

    assertEquals(GenerationFailureReason.PROVIDER_ERROR, generation.getFailureReason());
    verify(generationRepository).save(generation);
  }

  @Test
  @DisplayName(
      "given a generation that already ended, when complete then the generator is not called")
  void givenEndedGenerationWhenCompleteThenGeneratorNotCalled() {
    var generation = GenerationFactory.completed();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));

    service.complete(generation.getId());

    verify(generator, never()).generate(any());
    verify(generationRepository, never()).save(any());
  }

  @Test
  @DisplayName("given a generation that no longer exists, when complete then nothing happens")
  void givenMissingGenerationWhenCompleteThenNothingHappens() {
    var generationId = UUID.randomUUID();
    when(generationRepository.findById(generationId)).thenReturn(Optional.empty());

    service.complete(generationId);

    verify(generator, never()).generate(any());
    verify(generationRepository, never()).save(any());
  }
}
