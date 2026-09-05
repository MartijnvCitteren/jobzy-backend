package app.jobzy.api.application.service;

import static app.jobzy.api.testSupport.VacancyFactory.getFilledCreateCoreVacancyCommand;
import static org.mockito.Mockito.verify;

import app.jobzy.api.application.port.in.command.CreateCoreVacancyCommand;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.Vacancy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateVacancyServiceTest {

  @Mock private VacancyRepository vacancyRepository;

  @InjectMocks private CreateVacancyService createVacancyService;

  @Test
  @DisplayName("given valid command, when create core vacancy then vacancy is saved")
  void givenValidCommandWhenCreateCoreVacancyThenVacancyIsSaved() {
    CreateCoreVacancyCommand command = getFilledCreateCoreVacancyCommand().build();
    Vacancy vacancy = createVacancyService.createCoreVacancy(command);
    verify(vacancyRepository).save(vacancy);
  }
}
