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

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class CompanyInfoTokenService {
    private final PromtGenerator<CompanyInfoRequestDto> promtGenerator;
    private final UrlValidation urlValidation;
    private final AiClient aiClient;
    private final RedisJobInfoCacheSerive redisJobInfoCacheSerive;


    public CompanyInfoResponseToken getCompanyInfoResponseToken(CompanyInfoRequestDto companyInfoRequestDto) {
        if (!urlsAreValid(companyInfoRequestDto)) {
            throw new InvalidUrlException(companyInfoRequestDto.companyWebsite(),
                                          companyInfoRequestDto.exampleVacancyUrl());
        }

        Optional<UUID> optionalUUID = redisJobInfoCacheSerive.getUUID(companyInfoRequestDto.companyWebsite());
        if(optionalUUID.isPresent()) {
            return new CompanyInfoResponseToken(optionalUUID.get().toString());
        }

        PromptFormat prompt = promtGenerator.getPrompt(companyInfoRequestDto);
        AiCompanyInfo foundInfo = aiClient.getCompanyInfo(prompt, companyInfoRequestDto);
        UUID uuid = UUID.randomUUID();
        redisJobInfoCacheSerive.put(uuid, foundInfo);
        redisJobInfoCacheSerive.put(companyInfoRequestDto.companyWebsite(), uuid);
        System.out.println(redisJobInfoCacheSerive.getCompanyInfo(uuid).get());
        return new CompanyInfoResponseToken(uuid.toString());
    }

    private boolean urlsAreValid(CompanyInfoRequestDto companyInfoRequestDto) {
        if (ObjectUtils.isEmpty(companyInfoRequestDto.exampleVacancyUrl())) {
            return urlValidation.isValid(companyInfoRequestDto.companyWebsite());
        }
        return urlValidation.isValid(companyInfoRequestDto.companyWebsite()) && urlValidation.isValid(
                companyInfoRequestDto.exampleVacancyUrl());
    }



}
