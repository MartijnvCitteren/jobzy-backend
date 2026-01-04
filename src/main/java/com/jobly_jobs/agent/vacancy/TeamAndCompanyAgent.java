package com.jobly_jobs.agent.vacancy;

import com.jobly_jobs.agent.Agent;
import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.domain.dto.request.JobInfoRequestDto;
import com.jobly_jobs.prompt.dto.PromptValues;

public class TeamAndCompanyAgent implements Agent<PromptValues<JobInfoRequestDto>, CompanyAndTeamAiResponse> {

  @Override
  public CompanyAndTeamAiResponse execute(PromptValues<JobInfoRequestDto> prompt) {
    return null;
  }
}
