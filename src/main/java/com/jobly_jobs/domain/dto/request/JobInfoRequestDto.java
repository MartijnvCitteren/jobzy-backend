package com.jobly_jobs.domain.dto.request;

import com.jobly_jobs.domain.enums.Language;
import com.jobly_jobs.domain.enums.SeniorityLevel;
import com.jobly_jobs.domain.enums.WritingStyle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record JobInfoRequestDto(
        @Size(min = 2, max = 75, message = "Job Title must be between 2 and 75 characters") String jobTitle,
        @NotNull SeniorityLevel seniorityLevel,
        @Size(min = 20, max = 300, message = "Summary must be between 20 and 300 characters") String jobSummary,
        @Size(min = 10, max = 300, message = "Tasks must be between 10 and 300 characters") String tasks,
        @Size(min = 10, max = 300, message = "Skills must be between 10 and 300 characters") String skills,
        @Size(min = 10, max = 300, message = "Team description must be between 10 and 300 characters") String teamDescription,
        @NotNull WritingStyleRequestDto writingStyle,
        @Valid BenefitsRequestDto benefits,
        @Valid ContactInfoVacancyRequestDto contactInfo

) {
}
