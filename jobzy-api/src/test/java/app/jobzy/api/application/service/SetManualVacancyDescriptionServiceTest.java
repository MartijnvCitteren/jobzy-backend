package app.jobzy.api.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.jobzy.api.application.port.in.command.SetManualVacancyDescriptionCommand;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.VacancyNotFoundException;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescriptionSource;
import app.jobzy.api.testSupport.VacancyFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SetManualVacancyDescriptionServiceTest {

  @Mock private VacancyRepository vacancyRepository;

  @InjectMocks private SetManualVacancyDescriptionService service;

  @Test
  @DisplayName(
      "given an existing vacancy, when setManualDescription then description is replaced with"
          + " source manual and saved")
  void givenExistingVacancyWhenSetManualDescriptionThenDescriptionReplacedAndSaved() {
    var vacancy = VacancyFactory.getFilledCoreVacancy().build();
    when(vacancyRepository.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));
    var command =
        SetManualVacancyDescriptionCommand.builder()
            .vacancyId(vacancy.getId())
            .summary("Summary")
            .jobDescription("Job description")
            .tasks("Tasks")
            .whatWeOffer("What we offer")
            .aboutUs("About us")
            .build();

    var result = service.setManualDescription(command);

    assertEquals("Summary", result.getDescription().summary());
    assertEquals("Job description", result.getDescription().jobDescription());
    assertEquals("Tasks", result.getDescription().tasks());
    assertEquals("What we offer", result.getDescription().whatWeOffer());
    assertEquals("About us", result.getDescription().aboutUs());
    assertEquals(VacancyDescriptionSource.MANUAL, result.getDescription().source());
    verify(vacancyRepository).save(vacancy);
  }

  @Test
  @DisplayName("given an unknown vacancy id, when setManualDescription then throws and never saves")
  void givenUnknownVacancyIdWhenSetManualDescriptionThenThrowsAndNeverSaves() {
    var vacancyId = UUID.randomUUID();
    when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());
    var command = SetManualVacancyDescriptionCommand.builder().vacancyId(vacancyId).build();

    assertThrows(VacancyNotFoundException.class, () -> service.setManualDescription(command));

    verify(vacancyRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "given the repository fails to save, when setManualDescription then the exception"
          + " propagates unmodified")
  void givenRepositorySaveFailsWhenSetManualDescriptionThenExceptionPropagates() {
    var vacancy = VacancyFactory.getFilledCoreVacancy().build();
    when(vacancyRepository.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));
    var command = SetManualVacancyDescriptionCommand.builder().vacancyId(vacancy.getId()).build();
    var failure = new RuntimeException("database is down");
    doThrow(failure).when(vacancyRepository).save(vacancy);

    var thrown = assertThrows(RuntimeException.class, () -> service.setManualDescription(command));

    assertEquals(failure, thrown);
  }
}
