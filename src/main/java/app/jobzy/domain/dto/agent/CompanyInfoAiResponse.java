package app.jobzy.domain.dto.agent;

public record CompanyInfoAiResponse(
    String companyDescription,
    String companyGoal,
    String uspForEmployees,
    String toneOfVoice
) {

}
