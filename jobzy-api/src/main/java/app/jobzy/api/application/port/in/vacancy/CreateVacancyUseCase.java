package app.jobzy.api.application.port.in.vacancy;

import app.jobzy.api.application.port.in.vacancy.command.CreateCoreVacancyCommand;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface CreateVacancyUseCase {

  Vacancy createCoreVacancy(CreateCoreVacancyCommand createCoreVacancyCommand);
}
