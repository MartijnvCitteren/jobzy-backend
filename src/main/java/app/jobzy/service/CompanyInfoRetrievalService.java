package app.jobzy.service;

import app.jobzy.cache.CacheCompanyInfoService;
import app.jobzy.cache.CacheIdCompanyInfo;
import app.jobzy.agent.Agent;
import app.jobzy.domain.dto.agent.CompanyInfoAiResponse;
import app.jobzy.domain.dto.request.CompanyInfoRequestDto;
import app.jobzy.domain.dto.response.CompanyInfoResponseToken;
import app.jobzy.exceptions.InvalidUrlException;
import app.jobzy.prompt.dto.PromptValues;
import app.jobzy.prompt.generator.PromptGenerator;
import app.jobzy.validation.UrlValidation;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Log4j2
@RequiredArgsConstructor
@Service
public class CompanyInfoRetrievalService {

  private final PromptGenerator<CompanyInfoRequestDto> promptGenerator;
  private final UrlValidation urlValidation;
  private final Agent<PromptValues<CompanyInfoRequestDto>, CompanyInfoAiResponse> companyInfoAgent;
  private final CacheCompanyInfoService cacheCompanyInfoService;
  private final CacheIdCompanyInfo cacheIdCompanyInfo;


  public CompanyInfoResponseToken getCompanyInfoResponseToken(CompanyInfoRequestDto companyInfoRequestDto) {
    if (!urlsAreValid(companyInfoRequestDto)) {
      throw new InvalidUrlException(companyInfoRequestDto.companyWebsite(),
          companyInfoRequestDto.exampleVacancyUrl());
    }

    Optional<UUID> optionalUUID = cacheIdCompanyInfo.getUuid(companyInfoRequestDto.companyWebsite());
    if (optionalUUID.isPresent()) {
      return new CompanyInfoResponseToken(optionalUUID.get().toString());
    }

    PromptValues<CompanyInfoRequestDto> prompt = promptGenerator.getPrompt(companyInfoRequestDto);
    CompanyInfoAiResponse foundInfo = companyInfoAgent.execute(prompt);
    UUID uuid = storeInCacheAndGetUuid(companyInfoRequestDto, foundInfo);
    return new CompanyInfoResponseToken(uuid.toString());
  }

  private UUID storeInCacheAndGetUuid(CompanyInfoRequestDto companyInfoRequestDto, CompanyInfoAiResponse companyInfoAiResponse) {
    UUID uuid = UUID.randomUUID();
    cacheCompanyInfoService.putCompanyInfo(uuid, companyInfoAiResponse);
    cacheIdCompanyInfo.putCompanyWebsite(companyInfoRequestDto.companyWebsite(), uuid);
    return uuid;
  }

  private boolean urlsAreValid(CompanyInfoRequestDto companyInfoRequestDto) {
    if (ObjectUtils.isEmpty(companyInfoRequestDto.exampleVacancyUrl())) {
      return urlValidation.isValid(companyInfoRequestDto.companyWebsite());
    }
    return urlValidation.isValid(companyInfoRequestDto.companyWebsite()) && urlValidation.isValid(
        companyInfoRequestDto.exampleVacancyUrl());
  }
}
