package com.jobly_jobs.promt.generator;

import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.factory.CompanyInfoRequestDtoFactory;
import com.jobly_jobs.promt.dto.CompanyInfoSearchAction;
import com.jobly_jobs.promt.dto.PromptFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CompanyInfoPromptGeneratorTest {

    @InjectMocks
    private CompanyInfoPromptGenerator companyInfoPromtGenerator;

    @Test
    @DisplayName("Given valid company info request, when getting prompt, then return prompt format with correct values")
    void givenValidCompanyInfoRequest_whenGettingPrompt_thenReturnPromptFormatWithCorrectValues() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTask());
        assertNotNull(result.getScope());
        assertNotNull(result.getLimits());
        assertNotNull(result.getAction());

        assertTrue(result.getTask().contains(companyInfoRequestDto.companyWebsite()));
        assertTrue(result.getTask().contains(companyInfoRequestDto.exampleVacancyUrl()));
        assertTrue(result.getTask().contains("expert business analyst and brand strategist"));
        assertTrue(result.getTask().contains("Respond in the given format"));
    }

    @Test
    @DisplayName("Given company info request, when getting prompt, then scope contains correct information")
    void givenCompanyInfoRequest_whenGettingPrompt_thenScopeContainsCorrectInformation() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        assertNotNull(result.getScope());
        assertTrue(result.getScope().contains("company description"));
        assertTrue(result.getScope().contains("company goal"));
        assertTrue(result.getScope().contains("positioning as an employer"));
        assertTrue(result.getScope().contains("how the company communicates"));
    }

    @Test
    @DisplayName("Given company info request, when getting prompt, then action contains correct company details")
    void givenCompanyInfoRequest_whenGettingPrompt_thenActionContainsCorrectCompanyDetails() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        assertNotNull(result.getAction());
        CompanyInfoSearchAction action = assertInstanceOf(CompanyInfoSearchAction.class, result.getAction());

        assertEquals(companyInfoRequestDto.companyName(), action.getCompanyName());
        assertEquals(companyInfoRequestDto.country().toString(), action.getCountryLocated());
        assertEquals(companyInfoRequestDto.exampleVacancyUrl(), action.getUrlExampleVacancy());
    }

    @Test
    @DisplayName("Given company info request, when getting prompt, then limits are set with default values")
    void givenCompanyInfoRequest_whenGettingPrompt_thenLimitsAreSetWithDefaultValues() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        assertNotNull(result.getLimits());
        assertNotNull(result.getLimits().getMissingInfo());
        assertNotNull(result.getLimits().getFactuality());
        assertNotNull(result.getLimits().getMustDo());
        assertNotNull(result.getLimits().getMustAvoid());
    }

    @Test
    @DisplayName("Given company info request without vacancy url, when getting prompt, then prompt includes null " +
            "vacancy url")
    void givenCompanyInfoRequestWithoutVacancyUrl_whenGettingPrompt_thenPromptIncludesNullVacancyUrl() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto =
                CompanyInfoRequestDtoFactory.createCompanyInfoRequestDtoWithoutVacancyUrl();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTask());
        assertTrue(result.getTask().contains(companyInfoRequestDto.companyWebsite()));

        CompanyInfoSearchAction action = (CompanyInfoSearchAction) result.getAction();
        assertEquals(companyInfoRequestDto.companyName(), action.getCompanyName());
        assertEquals(companyInfoRequestDto.exampleVacancyUrl(), action.getUrlExampleVacancy());
    }

    @Test
    @DisplayName("Given company info request, when getting task, then task contains all required instructions")
    void givenCompanyInfoRequest_whenGettingTask_thenTaskContainsAllRequiredInstructions() {
        // Given
        CompanyInfoRequestDto companyInfoRequestDto = CompanyInfoRequestDtoFactory.createCompanyInfoRequestDto();

        // When
        PromptFormat result = companyInfoPromtGenerator.getPrompt(companyInfoRequestDto);

        // Then
        String task = result.getTask();
        assertNotNull(task);
        assertTrue(task.contains("Research the company"));
        assertTrue(task.contains("publicly available online sources"));
        assertTrue(task.contains("most important source for company information"));
        assertTrue(task.contains("Only use the above website addresses if you believe they are valid"));
    }
}

