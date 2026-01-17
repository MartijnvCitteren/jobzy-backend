package app.jobzy.mapper;

import app.jobzy.domain.dto.agent.BenefitsVacancyAiResponse;
import app.jobzy.domain.dto.agent.CompanyAndTeamAiResponse;
import app.jobzy.domain.dto.agent.CoreVacancyAiResponse;
import app.jobzy.domain.dto.response.GeneratedVacancyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

  GeneratedVacancyDto toVacancyDto(CompanyAndTeamAiResponse companyAndTeamAiResponse, CoreVacancyAiResponse coreResponse,
      BenefitsVacancyAiResponse benefitsVacancyAiResponse);

}
