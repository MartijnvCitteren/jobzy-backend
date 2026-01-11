package com.jobly_jobs.agent.vacancy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobly_jobs.agent.Agent;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.BenefitsVacancyAiResponse;
import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.domain.dto.agent.CoreVacancyAiResponse;
import com.jobly_jobs.exceptions.SystemException;
import com.jobly_jobs.prompt.dto.PromptValues;
import dev.toonformat.jtoon.JToon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class BenefitAgent implements Agent<PromptValues<JobInfo>, BenefitsVacancyAiResponse> {
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  @Override
  public BenefitsVacancyAiResponse execute(PromptValues<JobInfo> prompt) {
    try {
      String promptAsString = objectMapper.writeValueAsString(prompt);
      String toonPrompt = JToon.encode(promptAsString);
      return chatClient.prompt().user(toonPrompt).call().entity(BenefitsVacancyAiResponse.class);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new SystemException(e.getMessage());
    }
  }
}
