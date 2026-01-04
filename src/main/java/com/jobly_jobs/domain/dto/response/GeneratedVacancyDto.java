package com.jobly_jobs.domain.dto.response;

import lombok.Builder;

@Builder
public record GeneratedVacancyDto(
    String summary,
    String companyDescription,
    String teamDescription,
    String dayToDayDescription,
    String jobDescription,
    String jobUniqueSellingPoints,
    String requirements,
    String offer,
    String contactInformation
) {

}
