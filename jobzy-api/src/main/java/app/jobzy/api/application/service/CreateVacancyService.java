package app.jobzy.api.application.service;

import app.jobzy.api.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.domain.vacancy.valueobject.Location;
import app.jobzy.api.domain.vacancy.Vacancy;
import app.jobzy.api.domain.vacancy.valueobject.VacancyCategory;
import app.jobzy.api.domain.vacancy.valueobject.WorkplaceType;
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
