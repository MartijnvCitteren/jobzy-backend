package com.jobly_jobs.domain.dto.agent;

public record CompanyInfoAiResponse(
    String companyDescription,
    String companyGoal,
    String uspForEmployees,
    String toneOfVoice
) {

}
