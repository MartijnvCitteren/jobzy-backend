package app.jobzy.api.vacancy.adapter.in.web;

import app.jobzy.api.vacancy.adapter.in.web.mapper.vacancyCoreRequest.VacancyCoreRequestMapper;
import app.jobzy.api.vacancy.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.vacancy.domain.rest.GenerateVacancyDescriptionRequest;
import app.jobzy.api.vacancy.domain.rest.VacancyCategory;
import app.jobzy.api.vacancy.domain.rest.VacancyCoreRequest;
import app.jobzy.api.vacancy.domain.rest.VacancyDescriptionGeneration;
import app.jobzy.api.vacancy.domain.rest.VacancyListResponse;
import app.jobzy.api.vacancy.domain.rest.VacancyResponse;
import app.jobzy.api.vacancy.domain.rest.VacancyStatus;
import app.jobzy.api.vacancy.domain.rest.VacancyUpdateRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * No use case is wired in yet, so every operation falls through to {@link VacancyApi}'s default
 * methods, which respond with 501 Not Implemented.
 */
@Log4j2
@RestController
@RequiredArgsConstructor
public class VacancyController implements VacancyApi {
  private final VacancyCoreRequestMapper coreRequestMapper;
  private final CreateVacancyUseCase createVacancyUseCase;

  @Override
  public ResponseEntity<VacancyResponse> closeVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> createVacancy(VacancyCoreRequest vacancyCoreRequest) {
    log.info("Received creation request: {}", vacancyCoreRequest);
    var vacancyRequestDto = coreRequestMapper.toDto(vacancyCoreRequest);
    var coreVacancy = createVacancyUseCase.createCoreVacancy(vacancyRequestDto);
    var response =

  }

  @Override
  public ResponseEntity<Void> deleteVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> fillVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyDescriptionGeneration> generateVacancyDescription(
      UUID id, GenerateVacancyDescriptionRequest generateVacancyDescriptionRequest) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> getVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyDescriptionGeneration> getVacancyDescriptionGeneration(
      UUID id, UUID generationId) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyListResponse> listVacancies(
      UUID id,
      @Nullable String jobTitle,
      @Nullable List<VacancyStatus> status,
      @Nullable List<VacancyCategory> category,
      @Nullable LocalDate createdFrom,
      @Nullable LocalDate createdTo,
      @Nullable Integer openDaysMin,
      @Nullable Integer openDaysMax,
      @Nullable String contactPersonName,
      String sortBy,
      String sortDir,
      @Nullable String cursor,
      Integer limit) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> publishVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> updateVacancy(
      UUID id, VacancyUpdateRequest vacancyUpdateRequest) {
    return null;
  }
}
