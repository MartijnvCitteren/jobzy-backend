package app.jobzy.domain.dto.agent;

public record CoreVacancyAiResponse(
    String dayToDayDescription,
    String jobDescription,
    String jobUniqueSellingPoints,
    String requirements
) {

}
