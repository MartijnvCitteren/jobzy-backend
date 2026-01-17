package app.jobzy.agent.vacancy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.jobzy.agent.Agent;
import app.jobzy.domain.dto.JobInfo;
import app.jobzy.domain.dto.agent.CompanyInfoAiResponse;
import app.jobzy.domain.dto.agent.CoreVacancyAiResponse;
import app.jobzy.exceptions.SystemException;
import app.jobzy.prompt.dto.PromptValues;
import dev.toonformat.jtoon.JToon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions.UserLocation.Approximate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CoreVacancyAgent implements Agent<PromptValues<JobInfo>, CoreVacancyAiResponse> {
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  @Override
  public CoreVacancyAiResponse execute(PromptValues<JobInfo> prompt) {
    try {
      WebSearchOptions searchOptions = createWebSearchOptions();
      OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
      openAiChatOptions.setWebSearchOptions(searchOptions);

      String promptAsString = objectMapper.writeValueAsString(prompt);
      String toonPrompt = JToon.encode(promptAsString);
      log.debug(toonPrompt);

      return chatClient.prompt().options(openAiChatOptions).user(toonPrompt).call().entity(CoreVacancyAiResponse.class);
    } catch (JsonProcessingException e){
      log.error(e.getMessage());
      throw new SystemException(e.getMessage());
    }
  }


  private WebSearchOptions createWebSearchOptions() {
    Approximate approximate = new Approximate(null, null, "Europe", null);

    return new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,
        new WebSearchOptions.UserLocation("location", approximate));
  }
}
