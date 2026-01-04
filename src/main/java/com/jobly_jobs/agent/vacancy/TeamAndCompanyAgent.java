package com.jobly_jobs.agent.vacancy;

import com.jobly_jobs.agent.Agent;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.CompanyAndTeamAiResponse;
import com.jobly_jobs.prompt.dto.PromptValues;
import dev.toonformat.jtoon.JToon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class TeamAndCompanyAgent implements Agent<PromptValues<JobInfo>, CompanyAndTeamAiResponse> {

  private final ChatClient chatClient;

  @Override
  public CompanyAndTeamAiResponse execute(PromptValues<JobInfo> prompt) {
    String toonPrompt = JToon.encode(prompt);
    log.debug(toonPrompt);
    return chatClient.prompt().user(toonPrompt).call().entity(CompanyAndTeamAiResponse.class);
  }
}
