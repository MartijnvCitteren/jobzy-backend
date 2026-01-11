package com.jobly_jobs.domain.dto.agent;

public record CoreVacancyAiResponse(
    String dayToDayDescription,
    String jobDescription,
    String jobUniqueSellingPoints,
    String requirements
) {

}
