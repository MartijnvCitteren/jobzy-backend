package com.jobly_jobs.domain.dto.request;

import static com.jobly_jobs.validation.ValidationRegex.EMAIL_REGEX;
import static com.jobly_jobs.validation.ValidationRegex.PHONE_REGEX;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactInfoVacancyRequestDto(
    @Size(max = 25, message = "Name length should be shorter than 25 characters.") String name,
    @Size(max = 50, message = "Email length should be shorter than 50 characters.") @Pattern(regexp = EMAIL_REGEX
        , message = "The e-mailadres looks incorrect please check it.") String mail,
    @Size(max = 15, message = "Phone number length should be shorter then 15 characters")
    @Pattern(regexp = PHONE_REGEX, message = "Your phone looks incorrect, please check if it has at least 10 chars does not use any other characters as numbers, '-', '+' or spaces.") String phoneNumber
) {

}
