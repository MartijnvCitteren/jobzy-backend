package app.jobzy.api.application.port.in;

import app.jobzy.api.application.port.in.command.SetManualVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface ManualVacancyDescriptionUseCase {

  Vacancy setManualDescription(SetManualVacancyDescriptionCommand command);
}
