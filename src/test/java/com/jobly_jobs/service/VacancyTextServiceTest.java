//package com.jobly_jobs.service;
//
//import com.jobly_jobs.agent.CompanyInfoAgent;
//import com.jobly_jobs.domain.entity.Prompt;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//
//@ExtendWith(MockitoExtension.class)
//class VacancyTextServiceTest {
//
//    @Mock
//    CompanyInfoAgent openAiClient;
//
//    @InjectMocks
//    VacancyTextService vacancyTextService;
//
//    @Test
//    void givenPrompt_whenGeneratedVacancyText_thenAiClientGetsCalled9Times() {
//        // given
//        Prompt prompt = Prompt.builder()
//                .companyDescription("companyDescription")
//                .teamDescription("teamDescription")
//                .dayToDayDescription("dayToDayDescription")
//                .jobDescription("jobDescription")
//                .jobUniqueSellingPoints("jobUniqueSellingPoints")
//                .requirements("requirements")
//                .offer("offer")
//                .contactInformation("contactInformation")
//                .summary("summary")
//                .build();
//
//        // when
//        when(openAiClient.getResponse(Mockito.anyString())).thenReturn("response");
//        vacancyTextService.generatedVacancyText(prompt);
//
//        // then
//        verify(openAiClient, Mockito.times(9)).getResponse(Mockito.anyString());
//    }
//}