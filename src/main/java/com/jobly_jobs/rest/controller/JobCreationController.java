package com.jobly_jobs.rest.controller;

import com.jobly_jobs.client.OpenAiClient;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.request.JobCreationRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.domain.dto.response.JobCreationResponseDto;
import com.jobly_jobs.facade.JobCreationFacade;
import com.jobly_jobs.service.CompanyInfoService;
import com.jobly_jobs.service.JobRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("job")
@RequiredArgsConstructor
@Log4j2
public class JobCreationController {
    private final JobCreationFacade jobCreationFacade;
    private final JobRequestService jobRequestService;
    private final CompanyInfoService companyInfoService;
    private final OpenAiClient openAiClient;

    @PostMapping("/create-company-info")
    public ResponseEntity<CompanyInfoResponseToken> sendCompanyInfo(@RequestBody @Valid CompanyInfoRequestDto companyInfoRequestDto) {
        var response = companyInfoService.createCompanyInfo(companyInfoRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<AiCompanyInfo> getCompanyInfoTest(){
        var response = openAiClient.getCompanyInfo();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/create/")
    public ResponseEntity<GeneratedVacancyDto> generateVacancyText(@RequestBody @Valid JobCreationRequestDto descriptionInputDto) {
        GeneratedVacancyDto generatedVacancy = jobCreationFacade.generateVacancyText(descriptionInputDto);
        return new ResponseEntity<>(generatedVacancy, HttpStatus.CREATED);
    }

    @GetMapping("/view/")
    public ResponseEntity<JobCreationResponseDto> viewVacancyRequest(@RequestParam long id) {
        JobCreationResponseDto jobCreationResponseDto = jobRequestService.getJobRequest(id);
        return new ResponseEntity<>(jobCreationResponseDto, HttpStatus.OK);
    }

}
