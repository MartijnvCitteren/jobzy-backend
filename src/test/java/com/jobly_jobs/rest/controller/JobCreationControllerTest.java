//package com.jobly_jobs.rest.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
//import com.jobly_jobs.domain.dto.response.CompanyInfoResponseToken;
//import com.jobly_jobs.domain.enums.Country;
//import com.jobly_jobs.facade.JobCreationFacade;
//import com.jobly_jobs.factory.GeneralJobInfoDtoFactory;
//import com.jobly_jobs.factory.GeneratedVacancyDtoFactory;
//import com.jobly_jobs.factory.JobCreationRequestDtoFactory;
//import com.jobly_jobs.service.CompanyInfoTokenService;
//import com.jobly_jobs.service.JobRequestService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.util.stream.Stream;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(JobCreationController.class)
//@AutoConfigureMockMvc
//class JobCreationControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @MockitoBean
//    private JobCreationFacade jobCreationFacade;
//
//    @MockitoBean
//    private JobRequestService jobRequestService;
//
//    @MockitoBean
//    private CompanyInfoTokenService companyInfoService;
//
//    @Test
//    void givenCorrectInput_whenCreate_thenReturnsStatusCreated() throws Exception {
//        // given
//        var generalInfo = GeneralJobInfoDtoFactory.createGeneralInfoDto().build();
//        var creationRequest = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
//                .generalInfo(generalInfo)
//                .build();
//        var generatedVacancyText = GeneratedVacancyDtoFactory.createGeneratedVacancyDto().build();
//        when(jobCreationFacade.generateVacancyText(creationRequest)).thenReturn(generatedVacancyText);
//
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create/")
//                                .contentType("application/json")
//                                .content(convertToJsonString(creationRequest))).andExpect(status().isCreated());
//        verify(jobCreationFacade, times(1)).generateVacancyText(creationRequest);
//    }
//
//    @Test
//    void givenRequestWithoutGeneralInfo_whenCreate_thenReturnsStatusIsBadRequest() throws Exception {
//        // given
//        var jobCreationRequestDto = JobCreationRequestDtoFactory.createJobDescriptionInputDto()
//                .generalInfo(null)
//                .build();
//
//        //when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create/")
//                                .contentType("application/json")
//                                .content(convertToJsonString(jobCreationRequestDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("{\"generalInfo\":\"must not be null\"}"));
//    }
//
//    // Tests for sendCompanyInfo() method
//    @Test
//    void givenValidCompanyInfo_whenSendCompanyInfo_thenReturnsStatusCreated() throws Exception {
//        // given
//        var companyInfoRequest = new CompanyInfoRequestDto(
//                "TechCorp",
//                "https://techcorp.com",
//                Country.THE_NETHERLANDS,
//                "https://techcorp.com/careers/senior-dev"
//        );
//        var expectedResponse = new CompanyInfoResponseToken("test-token-123");
//        when(companyInfoService.createCompanyInfo(any(CompanyInfoRequestDto.class))).thenReturn(expectedResponse);
//
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
//                        .contentType("application/json")
//                        .content(convertToJsonString(companyInfoRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.token").value("test-token-123"));
//
//        verify(companyInfoService, times(1)).createCompanyInfo(any(CompanyInfoRequestDto.class));
//    }
//
//    @ParameterizedTest
//    @MethodSource("provideInvalidCompanyInfoRequests")
//    void givenInvalidCompanyInfo_whenSendCompanyInfo_thenReturnsBadRequest(
//            CompanyInfoRequestDto request,
//            String expectedField,
//            String expectedMessagePart
//    ) throws Exception {
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
//                        .contentType("application/json")
//                        .content(convertToJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$." + expectedField).exists())
//                .andExpect(jsonPath("$." + expectedField).value(org.hamcrest.Matchers.containsString(expectedMessagePart)));
//
//        verify(companyInfoService, times(0)).createCompanyInfo(any(CompanyInfoRequestDto.class));
//    }
//
//    private static Stream<Arguments> provideInvalidCompanyInfoRequests() {
//        return Stream.of(
//                // Company name too short (empty string)
//                Arguments.of(
//                        new CompanyInfoRequestDto("", "https://test.com", Country.THE_NETHERLANDS, null),
//                        "companyName",
//                        "Company name must be between 1 and 50 characters"
//                ),
//                // Company name too long (51 characters)
//                Arguments.of(
//                        new CompanyInfoRequestDto("A".repeat(51), "https://test.com", Country.THE_NETHERLANDS, null),
//                        "companyName",
//                        "Company name must be between 1 and 50 characters"
//                ),
//                // Website too short (empty string)
//                Arguments.of(
//                        new CompanyInfoRequestDto("TechCorp", "", Country.THE_NETHERLANDS, null),
//                        "companyWebsite",
//                        "website must be between 1 and 50 characters"
//                ),
//                // Website too long (51 characters)
//                Arguments.of(
//                        new CompanyInfoRequestDto("TechCorp", "https://" + "a".repeat(51), Country.THE_NETHERLANDS, null),
//                        "companyWebsite",
//                        "website must be between 1 and 50 characters"
//                ),
//                // Country is null
//                Arguments.of(
//                        new CompanyInfoRequestDto("TechCorp", "https://test.com", null, null),
//                        "country",
//                        "must not be null"
//                ),
//                // Example vacancy URL too long (101 characters)
//                Arguments.of(
//                        new CompanyInfoRequestDto("TechCorp", "https://test.com", Country.THE_NETHERLANDS, "https://" + "a".repeat(101)),
//                        "exampleVacancyUrl",
//                        "url to example vacancy should be less then 100 characters"
//                )
//        );
//    }
//
//    @Test
//    void givenMinimalValidCompanyInfo_whenSendCompanyInfo_thenReturnsStatusCreated() throws Exception {
//        // given - minimal valid request (without optional exampleVacancyUrl)
//        var companyInfoRequest = new CompanyInfoRequestDto(
//                "Corp",
//                "test.com",
//                Country.BELGIUM,
//                null
//        );
//        var expectedResponse = new CompanyInfoResponseToken("minimal-token");
//        when(companyInfoService.createCompanyInfo(any(CompanyInfoRequestDto.class))).thenReturn(expectedResponse);
//
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
//                        .contentType("application/json")
//                        .content(convertToJsonString(companyInfoRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.token").value("minimal-token"));
//
//        verify(companyInfoService, times(1)).createCompanyInfo(any(CompanyInfoRequestDto.class));
//    }
//
//    @Test
//    void givenMaxLengthValidCompanyInfo_whenSendCompanyInfo_thenReturnsStatusCreated() throws Exception {
//        // given - max length valid request
//        var companyInfoRequest = new CompanyInfoRequestDto(
//                "A".repeat(50),  // exactly 50 characters
//                "B".repeat(50),  // exactly 50 characters
//                Country.GERMANY,
//                "C".repeat(100)  // exactly 100 characters
//        );
//        var expectedResponse = new CompanyInfoResponseToken("max-length-token");
//        when(companyInfoService.createCompanyInfo(any(CompanyInfoRequestDto.class))).thenReturn(expectedResponse);
//
//        // when & then
//        mockMvc.perform(MockMvcRequestBuilders.post("/create-company-info")
//                        .contentType("application/json")
//                        .content(convertToJsonString(companyInfoRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.token").value("max-length-token"));
//
//        verify(companyInfoService, times(1)).createCompanyInfo(any(CompanyInfoRequestDto.class));
//    }
//
//
//    private String convertToJsonString(Object object) {
//        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
//        try {
//            return objectWriter.writeValueAsString(object);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//}