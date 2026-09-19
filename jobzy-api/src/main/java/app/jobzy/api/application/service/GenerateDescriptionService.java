package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.vacancy.GenerateDescriptionUseCase;
import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;

public class GenerateDescriptionService implements GenerateDescriptionUseCase {

  @Override
  public VacancyDescription generateVacancy(GenerateVacancyDescriptionCommand command) {

    return null;
  }
}
