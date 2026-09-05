package app.jobzy.api.adapter.in.rest.vacancy;

import app.jobzy.api.adapter.in.rest.vacancy.mapper.request.VacancyCoreRequestMapper;
import app.jobzy.api.adapter.in.rest.vacancy.mapper.response.VacancyResponseMapper;
import app.jobzy.api.application.port.in.CreateVacancyUseCase;
import app.jobzy.api.vacancy.adapter.in.rest.VacancyApi;
import app.jobzy.api.vacancy.adapter.in.web.contract.GenerateVacancyDescriptionRequest;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCategory;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyCoreRequest;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionGeneration;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionRequest;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyDescriptionResponse;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyListResponse;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyResponse;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyStatus;
import app.jobzy.api.vacancy.adapter.in.web.contract.VacancyUpdateRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Only {@link #createVacancy} is wired up so far; every other operation falls through to {@link
 * VacancyApi}'s default methods, which respond with 501 Not Implemented.
 */
@Log4j2
@RestController
@RequiredArgsConstructor
public class VacancyController implements VacancyApi {
  private final VacancyCoreRequestMapper coreRequestMapper;
  private final VacancyResponseMapper responseMapper;
  private final CreateVacancyUseCase createVacancyUseCase;

  @Override
  public ResponseEntity<VacancyResponse> closeVacancy(UUID id) {
    return null;
  }

  @Override
  public ResponseEntity<VacancyResponse> createVacancy(VacancyCoreRequest vacancyCoreRequest) {
    log.info("Received creation request: {}", vacancyCoreRequest);
    var createCoreVacancyCommand = coreRequestMapper.toCommand(vacancyCoreRequest);
    var coreVacancy = createVacancyUseCase.createCoreVacancy(createCoreVacancyCommand);
    var response = responseMapper.toVacancyResponse(coreVacancy);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
  public ResponseEntity<VacancyDescriptionResponse> manualVacancyDescription(
      UUID id, VacancyDescriptionRequest vacancyDescriptionRequest) {
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
