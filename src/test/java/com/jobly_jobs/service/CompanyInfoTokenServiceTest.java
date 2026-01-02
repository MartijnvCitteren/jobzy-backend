package com.jobly_jobs.service;

import com.jobly_jobs.cache.CacheCompanyService;
import com.jobly_jobs.client.AiClient;
import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.exceptions.InvalidUrlException;
import com.jobly_jobs.factory.AiCompanyInfoFactory;
import com.jobly_jobs.factory.CompanyInfoRequestDtoFactory;
import com.jobly_jobs.promt.dto.PromptFormat;
import com.jobly_jobs.promt.generator.PromptGenerator;
import com.jobly_jobs.validation.UrlValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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

@ExtendWith(MockitoExtension.class)
class CompanyInfoTokenServiceTest {

    @Mock
    private PromptGenerator<CompanyInfoRequestDto> promptGenerator;

    @Mock
    private UrlValidation urlValidation;

    @Mock
    private AiClient aiClient;

    @Mock
    private CacheCompanyService cacheCompanyService;

    @InjectMocks
    private CompanyInfoTokenService companyInfoTokenService;

    @Captor
    private ArgumentCaptor<UUID> uuidCaptor;

    @Captor
    private ArgumentCaptor<AiCompanyInfo> aiCompanyInfoCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private CompanyInfoRequestDto companyInfoRequestDto;
    private AiCompanyInfo aiCompanyInfo;
    private PromptFormat promptFormat;

    @BeforeEach
    void setUp() {
        companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();
        aiCompanyInfo = AiCompanyInfoFactory.createAiCompanyInfo();
        promptFormat = mock(PromptFormat.class);
    }

    @Test
    @DisplayName("given valid company info request with valid URLs, when getting token, then return token successfully")
    void givenValidCompanyInfoRequest_whenGettingToken_thenReturnTokenSuccessfully() {
        // Given
        when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
        when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
        when(cacheCompanyService.getUUID(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.empty());
        when(promptGenerator.getPrompt(companyInfoRequestDto)).thenReturn(promptFormat);
        when(aiClient.getCompanyInfo(promptFormat, companyInfoRequestDto)).thenReturn(aiCompanyInfo);

        // When
        companyInfoTokenService.getCompanyInfoResponseToken(companyInfoRequestDto);

        // Then
        verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
        verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
        verify(promptGenerator).getPrompt(companyInfoRequestDto);
        verify(aiClient).getCompanyInfo(promptFormat, companyInfoRequestDto);
        verify(cacheCompanyService).put(any(UUID.class), any(AiCompanyInfo.class));
        verify(cacheCompanyService).put(any(String.class), any(UUID.class));
    }

    @Test
    @DisplayName("given company info request with invalid website URL, when getting token, then throw " +
            "InvalidUrlException")
    void givenInvalidWebsiteUrl_whenGettingToken_thenThrowInvalidUrlException() {
        // Given
        when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(false);

        // When & Then
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                                                     () -> companyInfoTokenService.getCompanyInfoResponseToken(
                                                             companyInfoRequestDto));

        assertNotNull(exception);
        verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
        verify(promptGenerator, never()).getPrompt(any());
        verify(aiClient, never()).getCompanyInfo(any(), any());
        verify(cacheCompanyService, never()).put(any(UUID.class), any(AiCompanyInfo.class));
        verify(cacheCompanyService, never()).put(any(String.class), any(UUID.class));
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
                                                     () -> companyInfoTokenService.getCompanyInfoResponseToken(
                                                             companyInfoRequestDto));

        assertNotNull(exception);
        verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
        verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
        verify(promptGenerator, never()).getPrompt(any());
        verify(aiClient, never()).getCompanyInfo(any(), any());
    }

    @Test
    @DisplayName("given company info request without vacancy URL, when URLs are valid, then return token successfully")
    void givenCompanyInfoRequestWithoutVacancyUrl_whenUrlsAreValid_thenReturnTokenSuccessfully() {
        // Given
        CompanyInfoRequestDto requestWithoutVacancy =
                CompanyInfoRequestDtoFactory.createCompanyInfoRequestDtoWithoutVacancyUrl();
        when(urlValidation.isValid(requestWithoutVacancy.companyWebsite())).thenReturn(true);
        when(cacheCompanyService.getUUID(requestWithoutVacancy.companyWebsite())).thenReturn(Optional.empty());
        when(promptGenerator.getPrompt(requestWithoutVacancy)).thenReturn(promptFormat);
        when(aiClient.getCompanyInfo(promptFormat, requestWithoutVacancy)).thenReturn(aiCompanyInfo);

        // When
        companyInfoTokenService.getCompanyInfoResponseToken(requestWithoutVacancy);

        // Then
        verify(urlValidation, times(1)).isValid(requestWithoutVacancy.companyWebsite());
        verify(urlValidation, never()).isValid(null);
        verify(promptGenerator).getPrompt(requestWithoutVacancy);
        verify(aiClient).getCompanyInfo(promptFormat, requestWithoutVacancy);
    }

    @Test
    @DisplayName("given company info already cached, when getting token, then return cached token without AI call")
    void givenCompanyInfoAlreadyCached_whenGettingToken_thenReturnCachedTokenWithoutAiCall() {
        // Given
        UUID cachedUuid = UUID.randomUUID();
        when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
        when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
        when(cacheCompanyService.getUUID(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.of(cachedUuid));

        // When
        companyInfoTokenService.getCompanyInfoResponseToken(companyInfoRequestDto);

        // Then
        verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
        verify(urlValidation).isValid(companyInfoRequestDto.exampleVacancyUrl());
        verify(cacheCompanyService).getUUID(companyInfoRequestDto.companyWebsite());
        verifyNoInteractions(promptGenerator);
        verifyNoInteractions(aiClient);
        verifyNoMoreInteractions(cacheCompanyService);
    }

    @Test
    @DisplayName("given valid request, when storing in cache, then store both UUID and company info correctly")
    void givenValidRequest_whenStoringInCache_thenStoreBothUuidAndCompanyInfoCorrectly() {
        // Given
        when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(true);
        when(urlValidation.isValid(companyInfoRequestDto.exampleVacancyUrl())).thenReturn(true);
        when(cacheCompanyService.getUUID(companyInfoRequestDto.companyWebsite())).thenReturn(Optional.empty());
        when(promptGenerator.getPrompt(companyInfoRequestDto)).thenReturn(promptFormat);
        when(aiClient.getCompanyInfo(promptFormat, companyInfoRequestDto)).thenReturn(aiCompanyInfo);

        // When
        CompanyInfoResponseToken result = companyInfoTokenService.getCompanyInfoResponseToken(companyInfoRequestDto);

        // Then
        verify(cacheCompanyService).put(uuidCaptor.capture(), aiCompanyInfoCaptor.capture());
        verify(cacheCompanyService).put(stringCaptor.capture(), uuidCaptor.capture());

        UUID storedUuid = uuidCaptor.getAllValues().getFirst();
        AiCompanyInfo storedCompanyInfo = aiCompanyInfoCaptor.getValue();
        String storedWebsite = stringCaptor.getValue();

        assertEquals(result.token(), storedUuid.toString());
        assertEquals(aiCompanyInfo, storedCompanyInfo);
        assertEquals(companyInfoRequestDto.companyWebsite(), storedWebsite);
    }


    @Test
    @DisplayName("given invalid url, when getting token, then throw InvalidUrlException")
    void givenBothUrlsInvalid_whenGettingToken_thenThrowInvalidUrlException() {
        // Given
        when(urlValidation.isValid(companyInfoRequestDto.companyWebsite())).thenReturn(false);

        // When & Then
        assertThrows(InvalidUrlException.class,
                     () -> companyInfoTokenService.getCompanyInfoResponseToken(companyInfoRequestDto));

        verify(urlValidation).isValid(companyInfoRequestDto.companyWebsite());
        verifyNoInteractions(promptGenerator);
        verifyNoInteractions(aiClient);
    }

}

