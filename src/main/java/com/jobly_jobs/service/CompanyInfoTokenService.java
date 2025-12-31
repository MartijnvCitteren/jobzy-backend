package com.jobly_jobs.service;

import com.jobly_jobs.client.AiClient;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.exceptions.InvalidUrlException;
import com.jobly_jobs.promt.PromtGenerator;
import com.jobly_jobs.promt.dto.PromptFormat;
import com.jobly_jobs.validation.UrlValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class CompanyInfoTokenService {
    private final PromtGenerator<CompanyInfoRequestDto> promtGenerator;
    private final UrlValidation urlValidation;
    private final AiClient aiClient;


    public CompanyInfoResponseToken getCompanyInfoResponseToken(CompanyInfoRequestDto companyInfoRequestDto) {
        if(!urlsAreValid(companyInfoRequestDto)){
            throw new InvalidUrlException(companyInfoRequestDto.companyWebsite(),
                                          companyInfoRequestDto.exampleVacancyUrl());
        }

        PromptFormat prompt = promtGenerator.getPrompt(companyInfoRequestDto);
        AiCompanyInfo foundInfo = aiClient.getCompanyInfo(prompt, companyInfoRequestDto);
        System.out.println(foundInfo);
        UUID uuid = UUID.randomUUID();
        return new CompanyInfoResponseToken(uuid.toString());
    }

    private boolean urlsAreValid(CompanyInfoRequestDto companyInfoRequestDto) {
        if(ObjectUtils.isEmpty(companyInfoRequestDto.exampleVacancyUrl())){
            return urlValidation.isValid(companyInfoRequestDto.companyWebsite());
        }
        return urlValidation.isValid(companyInfoRequestDto.companyWebsite()) && urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl());
    }

}
