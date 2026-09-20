package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.vacancy.GenerateDescriptionUseCase;
import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class GenerateDescriptionService implements GenerateDescriptionUseCase {
  private final VacancyRepository vacancyRepository;

  @Override
  public VacancyDescription generateVacancy(GenerateVacancyDescriptionCommand command) {


    return null;
  }

}
