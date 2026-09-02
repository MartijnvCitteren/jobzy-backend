package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.application.port.in.command.CreateVacancyCommand;
import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
import org.springframework.stereotype.Service;

@Service
public class CreateVacancyService implements CreateVacancyUseCase {

  @Override
  public Vacancy createCoreVacancy(CreateVacancyCommand createVacancyCommand) {
    return Vacancy.createCore(
        createVacancyCommand.jobTitle(),
        VacancyCategory.valueOf(createVacancyCommand.category().name()),
        new Location(
            createVacancyCommand.location().country(), createVacancyCommand.location().city()),
        WorkplaceType.valueOf(createVacancyCommand.workplaceType().name()),
        createVacancyCommand.hoursPerWeek());
  }
}
