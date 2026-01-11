package com.jobly_jobs.rest.controller;

import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.service.CompanyInfoRetrievalService;
import com.jobly_jobs.service.VacancyCreationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "http://localhost:3000")
public class JobCreationController {

  private final CompanyInfoRetrievalService companyInfoRetrievalService;
  private final VacancyCreationService vacancyCreationService;

  @PostMapping("/create-company-info")
  public ResponseEntity<CompanyInfoResponseToken> createCompanyInfo(
      @RequestBody @Valid CompanyInfoRequestDto companyInfoRequestDto) {
    log.debug("Received companyInfoRequestDto {}", companyInfoRequestDto);
    var response = companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/create-vacancy")
  public ResponseEntity<GeneratedVacancyDto> createVacancyText(
      @RequestBody @Valid JobInfoRequestDto jobInfoRequestDto, @RequestHeader("requestId") UUID requestId) {
    GeneratedVacancyDto generatedVacancy = vacancyCreationService.createVacancy(jobInfoRequestDto, requestId);
    return new ResponseEntity<>(generatedVacancy, HttpStatus.CREATED);
  }
}
