package app.jobzy.api.vacancy.application.port.in;

import app.jobzy.api.vacancy.domain.rest.VacancyCoreRequest;
import app.jobzy.api.vacancy.domain.rest.VacancyResponse;
import org.springframework.http.ResponseEntity;

public interface CreateVacancyUseCase {

  ResponseEntity<VacancyResponse> createCoreVacancy(VacancyCoreRequest vacancyCoreRequest);

}
