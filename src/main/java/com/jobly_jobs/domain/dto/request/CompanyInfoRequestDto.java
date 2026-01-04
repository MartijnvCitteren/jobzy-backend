package com.jobly_jobs.domain.dto.request;

import static com.jobly_jobs.validation.ValidationRegex.VALID_WEBSITE_REGEX;

import com.jobly_jobs.domain.enums.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//@formatter:off
public record CompanyInfoRequestDto(
    @Size(min = 1, max = 50, message = "Company name must be between 1 and 50 characters")
    String companyName,

    @Pattern(regexp = VALID_WEBSITE_REGEX, message = "website address should look like 'www.example.com'")
    @Size(min = 1, max = 50, message = "website must be between 1 and 50 characters")
    @NotBlank
    String companyWebsite,

    @NotNull
    Country country,

    @Pattern(regexp = VALID_WEBSITE_REGEX, message = "Vacancy url should start with 'www.example.com'")
    @Size(max = 100, message = "url to example vacancy should be less then 100 characters")
    String exampleVacancyUrl
) {}
