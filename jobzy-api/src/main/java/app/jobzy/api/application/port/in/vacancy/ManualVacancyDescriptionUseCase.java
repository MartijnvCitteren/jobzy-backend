package app.jobzy.api.application.port.in.vacancy;

import app.jobzy.api.application.port.in.vacancy.command.SetManualVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface ManualVacancyDescriptionUseCase {

  Vacancy setManualDescription(SetManualVacancyDescriptionCommand command);
}
