package com.jobly_jobs.mapper;

import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.domain.dto.agent.CoreVacancyAiResponse;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

  GeneratedVacancyDto toVacancyDto(CompanyAndTeamAiResponse companyAndTeamAiResponse, CoreVacancyAiResponse coreResponse);

}
