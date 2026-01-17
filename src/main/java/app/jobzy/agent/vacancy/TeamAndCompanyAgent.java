package app.jobzy.agent.vacancy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.jobzy.agent.Agent;
import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.agent.CompanyAndTeamAiResponse;
import app.jobzy.exceptions.SystemException;
import app.jobzy.prompt.dto.PromptValues;
import dev.toonformat.jtoon.JToon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class TeamAndCompanyAgent implements Agent<PromptValues<JobInfo>, CompanyAndTeamAiResponse> {
  private final ObjectMapper objectMapper;
  private final ChatClient chatClient;

  @Override
  public CompanyAndTeamAiResponse execute(PromptValues<JobInfo> prompt) {
    try {
      String promptAsString = objectMapper.writeValueAsString(prompt);
      String toonPrompt = JToon.encode(promptAsString);
      return chatClient.prompt().user(toonPrompt).call().entity(CompanyAndTeamAiResponse.class);
    } catch (JsonProcessingException e){
      log.error(e.getMessage());
      throw new SystemException(e.getMessage());
    }
  }
}
