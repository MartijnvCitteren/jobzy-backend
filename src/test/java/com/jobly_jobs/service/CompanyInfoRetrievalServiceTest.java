package com.jobly_jobs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jobly_jobs.cache.CacheCompanyInfoService;
import com.jobly_jobs.cache.CacheIdCompanyInfo;
import com.jobly_jobs.agent.Agent;
import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.exceptions.InvalidUrlException;
import com.jobly_jobs.factory.AiCompanyInfoFactory;
import com.jobly_jobs.factory.CompanyInfoRequestDtoFactory;
import com.jobly_jobs.prompt.dto.PromptValues;
import com.jobly_jobs.prompt.generator.PromptGenerator;
import com.jobly_jobs.validation.UrlValidation;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyInfoRetrievalServiceTest {

  @Mock
  private PromptGenerator<CompanyInfoRequestDto> promptGenerator;

  @Mock
  private UrlValidation urlValidation;

  @Mock
  private Agent agent;

  @Mock
  private CacheCompanyInfoService cacheCompanyInfoService;

  @Mock
  private CacheIdCompanyInfo cacheIdCompanyInfo;

  @InjectMocks
  private CompanyInfoRetrievalService companyInfoRetrievalService;

  @Captor
  private ArgumentCaptor<UUID> uuidCaptor;

  @Captor
  private ArgumentCaptor<CompanyInfoAiResponse> aiCompanyInfoCaptor;

  @Captor
  private ArgumentCaptor<String> stringCaptor;

  private CompanyInfoRequestDto companyInfoRequestDto;
  private CompanyInfoAiResponse companyInfoAiResponse;
  private PromptValues promptValues;

  @BeforeEach
  void setUp() {
    companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();
    companyInfoAiResponse = AiCompanyInfoFactory.createAiCompanyInfo();
    promptValues = mock(PromptValues.class);
  }

  @Test
  @DisplayName("given valid company info request with valid URLs, when getting token, then return token successfully")
  void givenValidCompanyInfoRequest_whenGettingToken_thenReturnTokenSuccessfully() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
    when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
    when(cacheIdCompanyInfo.getUuid(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.empty());
    when(promptGenerator.getPrompt(companyInfoRequestDto)).thenReturn(promptValues);
    when(agent.getCompanyInfo(promptValues, companyInfoRequestDto)).thenReturn(companyInfoAiResponse);

    // When
    companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto);

    // Then
    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
    verify(promptGenerator).getPrompt(companyInfoRequestDto);
    verify(agent).getCompanyInfo(promptValues, companyInfoRequestDto);
    verify(cacheCompanyInfoService).putCompanyInfo(any(UUID.class), any(CompanyInfoAiResponse.class));
    verify(cacheIdCompanyInfo).putCompanyWebsite(any(String.class), any(UUID.class));
  }

  @Test
  @DisplayName("given company info request with invalid website URL, when getting token, then throw " +
      "InvalidUrlException")
  void givenInvalidWebsiteUrl_whenGettingToken_thenThrowInvalidUrlException() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(false);

    // When & Then
    InvalidUrlException exception = assertThrows(InvalidUrlException.class,
        () -> companyInfoRetrievalService.getCompanyInfoResponseToken(
            companyInfoRequestDto));

    assertNotNull(exception);
    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verifyNoInteractions(promptGenerator);
    verifyNoInteractions(agent);
    verifyNoInteractions(cacheCompanyInfoService);
    verifyNoInteractions(cacheIdCompanyInfo);
  }

  @Test
  @DisplayName("given company info request with invalid vacancy URL, when getting token, then throw " +
      "InvalidUrlException")
  void givenInvalidVacancyUrl_whenGettingToken_thenThrowInvalidUrlException() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
    when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(false);

    // When & Then
    InvalidUrlException exception = assertThrows(InvalidUrlException.class,
        () -> companyInfoRetrievalService.getCompanyInfoResponseToken(
            companyInfoRequestDto));

    assertNotNull(exception);
    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
    verifyNoInteractions(promptGenerator);
    verifyNoInteractions(agent);
  }

  @Test
  @DisplayName("given company info request without vacancy URL, when URLs are valid, then return token successfully")
  void givenCompanyInfoRequestWithoutVacancyUrl_whenUrlsAreValid_thenReturnTokenSuccessfully() {
    // Given
    CompanyInfoRequestDto requestWithoutVacancy =
        CompanyInfoRequestDtoFactory.createCompanyInfoRequestDtoWithoutVacancyUrl();
    when(urlValidation.isValid(requestWithoutVacancy.companyWebsite())).thenReturn(true);
    when(cacheIdCompanyInfo.getUuid(requestWithoutVacancy.companyWebsite())).thenReturn(Optional.empty());
    when(promptGenerator.getPrompt(requestWithoutVacancy)).thenReturn(promptValues);
    when(agent.getCompanyInfo(promptValues, requestWithoutVacancy)).thenReturn(companyInfoAiResponse);

    // When
    companyInfoRetrievalService.getCompanyInfoResponseToken(requestWithoutVacancy);

    // Then
    verify(urlValidation, times(1)).isValid(requestWithoutVacancy.companyWebsite());
    verify(urlValidation, never()).isValid(null);
    verify(promptGenerator).getPrompt(requestWithoutVacancy);
    verify(agent).getCompanyInfo(promptValues, requestWithoutVacancy);
  }

  @Test
  @DisplayName("given company info already cached, when getting token, then return cached token without AI call")
  void givenCompanyInfoAlreadyCached_whenGettingToken_thenReturnCachedTokenWithoutAiCall() {
    // Given
    UUID cachedUuid = UUID.randomUUID();
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
    when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
    when(cacheIdCompanyInfo.getUuid(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.of(cachedUuid));

    // When
    companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto);

    // Then
    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
    verify(cacheIdCompanyInfo).getUuid(companyInfoRequestDto.companyWebsite());
    verifyNoInteractions(promptGenerator);
    verifyNoInteractions(agent);
    verifyNoMoreInteractions(cacheIdCompanyInfo);
    verifyNoInteractions(cacheCompanyInfoService);
  }

  @Test
  @DisplayName("given valid request, when storing in cache, then store both UUID and company info correctly")
  void givenValidRequest_whenStoringInCache_thenStoreBothUuidAndCompanyInfoCorrectly() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
    when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
    when(cacheIdCompanyInfo.getUuid(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.empty());
    when(promptGenerator.getPrompt(companyInfoRequestDto)).thenReturn(promptValues);
    when(agent.getCompanyInfo(promptValues, companyInfoRequestDto)).thenReturn(companyInfoAiResponse);

    // When
    CompanyInfoResponseToken result = companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto);

    // Then
    verify(cacheCompanyInfoService).putCompanyInfo(uuidCaptor.capture(), aiCompanyInfoCaptor.capture());
    verify(cacheIdCompanyInfo).putCompanyWebsite(stringCaptor.capture(), uuidCaptor.capture());

    UUID storedUuid = uuidCaptor.getAllValues().getFirst();
    CompanyInfoAiResponse storedCompanyInfo = aiCompanyInfoCaptor.getValue();
    String storedWebsite = stringCaptor.getValue();

    assertEquals(result.token(), storedUuid.toString());
    assertEquals(companyInfoAiResponse, storedCompanyInfo);
    assertEquals(companyInfoRequestDto.companyWebsite(), storedWebsite);
  }


  @Test
  @DisplayName("given invalid url, when getting token, then throw InvalidUrlException")
  void givenBothUrlsInvalid_whenGettingToken_thenThrowInvalidUrlException() {
    // Given
    when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(false);

    // When & Then
    assertThrows(InvalidUrlException.class,
        () -> companyInfoRetrievalService.getCompanyInfoResponseToken(companyInfoRequestDto));

    verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
    verifyNoInteractions(promptGenerator);
    verifyNoInteractions(agent);
  }

}

