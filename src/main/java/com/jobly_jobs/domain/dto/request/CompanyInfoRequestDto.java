package com.jobly_jobs.domain.dto.request;

import com.jobly_jobs.domain.enums.Country;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyInfoRequestDto(
        @Size(min = 1, max = 50, message = "Company name must be between 1 and 50 characters")
        String companyName,
        @Size(min = 1, max = 50, message = "website must be between 1 and 50 characters")
        String companyWebsite,
        @NotNull
        Country country,
        @Size(max = 100, message = "url to example vacancy should be less then 100 characters")
        String exampleVacancyUrl
) {
}
