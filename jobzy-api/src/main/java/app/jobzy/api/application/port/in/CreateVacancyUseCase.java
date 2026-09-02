package app.jobzy.api.application.port.in;

import app.jobzy.api.application.port.in.command.CreateVacancyCommand;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface CreateVacancyUseCase {

  Vacancy createCoreVacancy(CreateVacancyCommand createVacancyCommand);
}
