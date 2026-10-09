package app.jobzy.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.jobzy.api.application.port.in.command.StartVacancyDescriptionGenerationCommand;
import app.jobzy.api.application.port.out.VacancyDescriptionGenerationRepository;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.generation.VacancyDescriptionGenerationStarted;
import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.generation.valueobject.GenerationStatus;
import app.jobzy.api.domain.vacancy.VacancyNotFoundException;
import app.jobzy.api.testSupport.VacancyFactory;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class StartVacancyDescriptionGenerationServiceTest {

  private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 10, 0);

  @Mock private VacancyRepository vacancyRepository;
  @Mock private VacancyDescriptionGenerationRepository generationRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private StartVacancyDescriptionGenerationService service;

  @BeforeEach
  void setUp() {
    var clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    service =
        new StartVacancyDescriptionGenerationService(
            vacancyRepository, generationRepository, eventPublisher, clock);
  }

  @Test
  @DisplayName(
      "given an existing vacancy, when start then a pending generation with a snapshot of the"
          + " vacancy and the answers is saved and announced")
  void givenExistingVacancyWhenStartThenPendingGenerationSavedAndAnnounced() {
    var vacancy = VacancyFactory.getFilledCoreVacancy().build();
    when(vacancyRepository.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));
    var command =
        StartVacancyDescriptionGenerationCommand.builder()
            .vacancyId(vacancy.getId())
            .mostImportantTasks("Tasks")
            .team("Team")
            .whyNiceJob("Why")
            .build();

    var generation = service.start(command);

    assertEquals(GenerationStatus.PENDING, generation.getStatus());
    assertEquals(vacancy.getId(), generation.getVacancyId());
    assertEquals(NOW, generation.getRequestedAt());
    assertEquals(
        new GenerationInput(
            VacancyFactory.JOB_TITLE, VacancyFactory.LANGUAGE, "Tasks", "Team", "Why"),
        generation.getInput());
    verify(generationRepository).save(generation);
    verify(eventPublisher)
        .publishEvent(new VacancyDescriptionGenerationStarted(generation.getId()));
  }

  @Test
  @DisplayName("given an unknown vacancy, when start then throws and nothing is saved or announced")
  void givenUnknownVacancyWhenStartThenThrowsAndNothingSaved() {
    var vacancyId = UUID.randomUUID();
    when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());
    var command =
        StartVacancyDescriptionGenerationCommand.builder()
            .vacancyId(vacancyId)
            .mostImportantTasks("Tasks")
            .team("Team")
            .whyNiceJob("Why")
            .build();

    assertThrows(VacancyNotFoundException.class, () -> service.start(command));

    verify(generationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }
}
