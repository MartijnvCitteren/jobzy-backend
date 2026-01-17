package app.jobzy.domain.mapper;

import app.jobzy.domain.dto.response.GeneratedVacancyDto;
import app.jobzy.domain.entity.VacancyText;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VacancyTextMapper {

  public static VacancyText toVacancyText(GeneratedVacancyDto generatedVacancyDto) {
    VacancyText vacancyText = new VacancyText();
    vacancyText.setSummary(generatedVacancyDto.summary());
    vacancyText.setCompanyDescription(generatedVacancyDto.companyDescription());
    vacancyText.setTeamDescription(generatedVacancyDto.teamDescription());
    vacancyText.setDayToDayDescription(generatedVacancyDto.dayToDayDescription());
    vacancyText.setJobDescription(generatedVacancyDto.jobDescription());
    vacancyText.setJobUniqueSellingPoints(generatedVacancyDto.jobUniqueSellingPoints());
    vacancyText.setRequirements(generatedVacancyDto.requirements());
    vacancyText.setOffer(generatedVacancyDto.offer());
    vacancyText.setContactInformation(generatedVacancyDto.contactInformation());
    return vacancyText;
  }

}
