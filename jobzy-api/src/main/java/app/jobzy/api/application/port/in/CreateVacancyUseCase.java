package app.jobzy.api.application.port.in;

import app.jobzy.api.application.port.in.command.CreateCoreVacancyCommand;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface CreateVacancyUseCase {

  Vacancy createCoreVacancy(CreateCoreVacancyCommand createCoreVacancyCommand);
}
