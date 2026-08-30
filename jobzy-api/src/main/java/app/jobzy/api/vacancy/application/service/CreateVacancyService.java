package app.jobzy.api.vacancy.application.service;

import app.jobzy.api.vacancy.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.vacancy.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.vacancy.domain.Location;
import app.jobzy.api.vacancy.domain.Vacancy;
import app.jobzy.api.vacancy.domain.VacancyCategory;
import app.jobzy.api.vacancy.domain.WorkplaceType;
import org.springframework.stereotype.Service;

@Service
public class CreateVacancyService implements CreateVacancyUseCase {

  @Override
  public Vacancy createCoreVacancy(VacancyCoreRequestDto vacancyCoreRequestDto) {
    return Vacancy.createCore(
        vacancyCoreRequestDto.jobTitle(),
        VacancyCategory.valueOf(vacancyCoreRequestDto.category().name()),
        new Location(
            vacancyCoreRequestDto.location().country(), vacancyCoreRequestDto.location().city()),
        WorkplaceType.valueOf(vacancyCoreRequestDto.workplaceType().name()),
        vacancyCoreRequestDto.hoursPerWeek());
  }
}
