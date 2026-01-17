package app.jobzy.domain.dto.response;

import lombok.Builder;

//@formatting:off
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
