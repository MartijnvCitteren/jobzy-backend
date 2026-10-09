package app.jobzy.api.application.service;

import static app.jobzy.api.testSupport.GenerationFactory.REQUESTED_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.domain.generation.VacancyDescriptionGenerationNotFoundException;
import app.jobzy.api.domain.generation.valueobject.GenerationFailureReason;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.testSupport.GenerationFactory;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetVacancyDescriptionGenerationServiceTest {

  @Mock private VacancyDescriptionGenerationRepository generationRepository;

  private GetVacancyDescriptionGenerationService serviceAt(LocalDateTime now) {
    var clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    return new GetVacancyDescriptionGenerationService(generationRepository, clock);
  }

  @Test
  @DisplayName("given a generation of this vacancy, when get then it is returned")
  void givenGenerationOfThisVacancyWhenGetThenReturned() {
    var generation = GenerationFactory.completed();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));

    var result =
        serviceAt(REQUESTED_AT.plusHours(1)).get(generation.getVacancyId(), generation.getId());

    assertSame(generation, result);
  }

  @Test
  @DisplayName(
      "given a pending generation past its deadline, when get then it is reported as a timeout")
  void givenPendingGenerationPastDeadlineWhenGetThenReportedAsTimeout() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));

    var result =
        serviceAt(REQUESTED_AT.plusSeconds(91)).get(generation.getVacancyId(), generation.getId());

    assertEquals(GenerationStatus.FAILED, result.getStatus());
    assertEquals(GenerationFailureReason.TIMEOUT, result.getFailureReason());
  }

  @Test
  @DisplayName("given a generation of another vacancy, when get then not found")
  void givenGenerationOfAnotherVacancyWhenGetThenNotFound() {
    var generation = GenerationFactory.pending();
    when(generationRepository.findById(generation.getId())).thenReturn(Optional.of(generation));
    var service = serviceAt(REQUESTED_AT);
    var otherVacancyId = UUID.randomUUID();

    assertThrows(
        VacancyDescriptionGenerationNotFoundException.class,
        () -> service.get(otherVacancyId, generation.getId()));
  }

  @Test
  @DisplayName("given an unknown generation, when get then not found")
  void givenUnknownGenerationWhenGetThenNotFound() {
    var generationId = UUID.randomUUID();
    when(generationRepository.findById(generationId)).thenReturn(Optional.empty());
    var service = serviceAt(REQUESTED_AT);
    var vacancyId = UUID.randomUUID();

    assertThrows(
        VacancyDescriptionGenerationNotFoundException.class,
        () -> service.get(vacancyId, generationId));
  }
}
