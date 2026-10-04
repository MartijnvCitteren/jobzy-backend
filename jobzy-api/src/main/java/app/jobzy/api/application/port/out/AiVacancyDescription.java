package app.jobzy.api.application.port.out;

import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;

public interface AiVacancyDescription {

  VacancyDescription generateVacancyDescription(GenerateVacancyDescriptionCommand command);
}
