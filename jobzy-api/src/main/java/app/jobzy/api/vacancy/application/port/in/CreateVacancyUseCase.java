package app.jobzy.api.vacancy.application.port.in;

import app.jobzy.api.vacancy.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.vacancy.domain.Vacancy;

public interface CreateVacancyUseCase {

  Vacancy createCoreVacancy(VacancyCoreRequestDto vacancyCoreRequestDto);
}
