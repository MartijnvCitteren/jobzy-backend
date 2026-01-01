package com.jobly_jobs.client;

import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.promt.dto.PromptFormat;

public interface AiClient {
    GeneratedVacancyDto getResponse(String message);

    AiCompanyInfo getCompanyInfo(PromptFormat prompt, CompanyInfoRequestDto companyInfoRequestDto);
}
