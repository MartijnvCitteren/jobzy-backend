package app.jobzy.api.application.port.in;

import app.jobzy.api.application.port.in.dto.VacancyCoreRequestDto;
import app.jobzy.api.domain.vacancy.Vacancy;

public interface CreateVacancyUseCase {

  Vacancy createCoreVacancy(VacancyCoreRequestDto vacancyCoreRequestDto);
}
