package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.vacancy.GenerateDescriptionUseCase;
import app.jobzy.api.application.port.in.vacancy.command.GenerateVacancyDescriptionCommand;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.valueobject.VacancyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class GenerateDescriptionService implements GenerateDescriptionUseCase {
  private final VacancyRepository vacancyRepository;

  @Override
  public VacancyDescription generateVacancyDescription(GenerateVacancyDescriptionCommand command) {

    return null;
  }
}
