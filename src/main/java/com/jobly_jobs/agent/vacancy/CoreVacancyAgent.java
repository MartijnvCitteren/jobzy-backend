package com.jobly_jobs.agent.vacancy;

import com.jobly_jobs.agent.Agent;
import com.jobly_jobs.domain.dto.JobInfo;
import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;
import com.jobly_jobs.domain.dto.agent.CoreVacancyAiResponse;
import com.jobly_jobs.prompt.dto.PromptValues;
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

  @Override
  public CoreVacancyAiResponse execute(PromptValues<JobInfo> prompt) {
    WebSearchOptions searchOptions = createWebSearchOptions();
    OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
    openAiChatOptions.setWebSearchOptions(searchOptions);

    String toonPrompt = JToon.encode(prompt);
    log.debug(toonPrompt);

    return chatClient.prompt().options(openAiChatOptions).user(toonPrompt).call().entity(CoreVacancyAiResponse.class);
  }


  private WebSearchOptions createWebSearchOptions() {
    Approximate approximate = new Approximate(null, null, "Europe", null);

    return new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,
        new WebSearchOptions.UserLocation("location", approximate));
  }
}
