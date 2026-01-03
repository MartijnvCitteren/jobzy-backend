package com.jobly_jobs.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
import com.jobly_jobs.domain.enums.Country;
import com.jobly_jobs.facade.JobCreationFacade;
import com.jobly_jobs.factory.GeneralJobInfoDtoFactory;
import com.jobly_jobs.factory.GeneratedVacancyDtoFactory;
import com.jobly_jobs.factory.JobCreationRequestDtoFactory;
import com.jobly_jobs.service.CompanyInfoTokenService;
import com.jobly_jobs.service.JobRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobCreationController.class)
@AutoConfigureMockMvc
class JobCreationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private JobCreationFacade jobCreationFacade;

    @MockitoBean
    private JobRequestService jobRequestService;

    @MockitoBean
    private CompanyInfoTokenService companyInfoService;

    @Test
    @DisplayName("Given correct input, when creating job, then returns status created")
    void givenCorrectInput_whenCreate_thenReturnsStatusCreated() throws Exception {
        // given
        var generalInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
        var creationRequest = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
                .generalInfo(generalInfo)
                .build();
        var generatedVacancyText = GeneratedVacancyDtoFactory.createGeneratedVacancyDto().build();
        when(jobCreationFacade.generateVacancyText(creationRequest)).thenReturn(generatedVacancyText);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/create/")
                                .contentType("application/json")
                                .content(convertToJsonString(creationRequest))).andExpect(status().isCreated());
        verify(jobCreationFacade, times(1)).generateVacancyText(creationRequest);
    }

    @Test
    @DisplayName("Given request without general info, when creating job, then returns status bad request")
    void givenRequestWithoutGeneralInfo_whenCreate_thenReturnsStatusIsBadRequest() throws Exception {
        // given
        var jobCreationRequestDto = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
                .generalInfo(null)
                .build();

        //when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/create/")
                                .contentType("application/json")
                                .content(convertToJsonString(jobCreationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"generalInfo\":\"must not be null\"}"));
    }

    // Tests for sendCompanyInfo() method
    @ParameterizedTest
    @MethodSource("provideValidCompanyInfoRequests")
    @DisplayName("Given valid company info with various URL formats, when sending company info, then returns status " + "created")
    void givenValidCompanyInfo_whenSendCompanyInfo_thenReturnsStatusCreated(CompanyInfoRequestDto request,
                                                                            String testDescription) throws Exception {
        // given
        var expectedResponse = new CompanyInfoResponseToken("test-token-123");
        when(companyInfoService.getCompanyInfoResponseToken(any(CompanyInfoRequestDto.class))).thenReturn(
                expectedResponse);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
                                .contentType("application/json")
                                .content(convertToJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token-123"));

        verify(companyInfoService, times(1)).getCompanyInfoResponseToken(any(CompanyInfoRequestDto.class));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCompanyInfoRequests")
    @DisplayName("Given invalid company info with validation errors, when sending company info, then returns bad " +
            "request")
    void givenInvalidCompanyInfo_whenSendCompanyInfo_thenReturnsBadRequest(CompanyInfoRequestDto request,
                                                                           String expectedField,
                                                                           String expectedMessagePart)
            throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
                                .contentType("application/json")
                                .content(convertToJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + expectedField).exists())
                .andExpect(jsonPath("$." + expectedField).value(
                        org.hamcrest.Matchers.containsString(expectedMessagePart)));

        verifyNoInteractions(companyInfoService);
    }

    private String convertToJsonString(Object object) {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return objectWriter.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<Arguments> provideValidCompanyInfoRequests() {
        return Stream.of(
                // Valid URLs with https://
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://techcorp.com", Country.THE_NETHERLANDS, "https://techcorp.com/careers"),
                             "Valid URL with https and .com"),
                Arguments.of(
                        new CompanyInfoRequestDto("TechCorp", "https://www.techcorp.nl", Country.THE_NETHERLANDS, null),
                        "Valid URL with https, www and .nl"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "http://techcorp.be", Country.BELGIUM, null),
                             "Valid URL with http and .be"),
                Arguments.of(
                        new CompanyInfoRequestDto("TechCorp", "https://company-name.de/path/to/page", Country.GERMANY,
                                                  null), "Valid URL with https, dash in domain and path"),
                // Valid URLs with www.
                Arguments.of(new CompanyInfoRequestDto("Corp", "www.example.com", Country.THE_NETHERLANDS, null),
                             "Valid URL starting with www"), Arguments.of(
                        new CompanyInfoRequestDto("Corp", "www.test-company.io", Country.THE_NETHERLANDS,
                                                  "www.test-company.io/jobs"), "Valid URL with www, dash and .io"),
                // Various TLDs
                Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.eu", Country.THE_NETHERLANDS, null),
                             "Valid URL with .eu TLD"),
                Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.tech", Country.THE_NETHERLANDS, null),
                             "Valid URL with .tech TLD"),
                Arguments.of(new CompanyInfoRequestDto("Corp", "https://example.co.uk", Country.THE_NETHERLANDS, null),
                             "Valid URL with .co.uk TLD"),
                // Minimal and maximal valid lengths
                Arguments.of(new CompanyInfoRequestDto("C", "www.a.com", Country.BELGIUM, null),
                             "Minimal valid lengths"), Arguments.of(
                        new CompanyInfoRequestDto("A".repeat(50), "https://techcorp.com", Country.GERMANY,
                                                  "https://techcorp.com/jobs"), "Maximal valid lengths"));
    }

    private static Stream<Arguments> provideInvalidCompanyInfoRequests() {
        return Stream.of(
                // Company name validation
                Arguments.of(new CompanyInfoRequestDto("", "https://test.com", Country.THE_NETHERLANDS, null),
                             "companyName", "Company name must be between 1 and 50 characters"), Arguments.of(
                        new CompanyInfoRequestDto("A".repeat(51), "https://test.com", Country.THE_NETHERLANDS, null),
                        "companyName", "Company name must be between 1 and 50 characters"),

                // Website URL regex validation - invalid formats
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "not-a-url", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "ftp://test.com", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "test.com", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "www.test", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "http://test", Country.THE_NETHERLANDS, null),
                             "companyWebsite", "website address should look like 'www.example.com'"),

                // Country validation
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://test.com", null, null), "country",
                             "must not be null"),

                // Example vacancy URL regex validation
                Arguments.of(new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS,
                                                       "not-a-valid-url"), "exampleVacancyUrl",
                             "Vacancy url should start with 'www.example.com'"), Arguments.of(
                        new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS,
                                                  "ftp://test.com"), "exampleVacancyUrl",
                        "Vacancy url should start with 'www.example.com'"), Arguments.of(
                        new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS, "test.xyz"),
                        "exampleVacancyUrl", "Vacancy url should start with 'www.example.com'"));
    }

}

