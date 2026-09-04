package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.application.port.in.command.CreateCoreVacancyCommand;
import app.jobzy.api.application.port.out.VacancyRepository;
import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.valueobject.VacancyStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CreateVacancyService implements CreateVacancyUseCase {
  private final VacancyRepository vacancyRepository;

  @Override
  public Vacancy createCoreVacancy(CreateCoreVacancyCommand command) {
    var vacancy =
        Vacancy.builder()
            .jobTitle(command.jobTitle())
            .location(command.location())
            .category(command.category())
            .status(VacancyStatus.DRAFT)
            .hoursPerWeek(command.hoursPerWeek())
            .workplaceType(command.workplaceType())
            .build();

    log.info("Vacancy is created: {}", vacancy);

    return vacancy;
  }
}
