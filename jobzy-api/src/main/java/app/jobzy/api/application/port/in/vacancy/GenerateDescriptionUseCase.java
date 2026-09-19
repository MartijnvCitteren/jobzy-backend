package app.jobzy.api.application.port.in.vacancy;

import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;

public interface GenerateDescriptionUseCase {

  VacancyDescription generateVacancy(GenerateVacancyDescriptionCommand command);
}
