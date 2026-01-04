package com.jobly_jobs.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobly_jobs.domain.dto.agent.CompanyInfoAiResponse;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.exceptions.SystemException;
import com.jobly_jobs.prompt.dto.PromptValues;
import dev.toonformat.jtoon.JToon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions.UserLocation.Approximate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


@Service
@Log4j2
@RequiredArgsConstructor
public class CompanyInfoAgent implements Agent<PromptValues<CompanyInfoRequestDto>, CompanyInfoAiResponse> {
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  @Override
  public CompanyInfoAiResponse execute (PromptValues<CompanyInfoRequestDto> prompt) {
    try {
      WebSearchOptions webSearchOptions = createWebSearchOptions(prompt.getRequestObject());
      OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
      openAiChatOptions.setWebSearchOptions(webSearchOptions);

      String promptAsString = objectMapper.writeValueAsString(prompt);

      log.info(prompt.toString());
      String toonPrompt = JToon.encode(promptAsString);
      log.debug(toonPrompt);

      return chatClient.prompt().options(openAiChatOptions).user(toonPrompt).call().entity(CompanyInfoAiResponse.class);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new SystemException(e.getMessage());
    }
  }


  private WebSearchOptions createWebSearchOptions(CompanyInfoRequestDto companyInfoRequestDto) {
    Approximate approximate;
    if (ObjectUtils.isEmpty(companyInfoRequestDto.country())) {
      approximate = null;
    } else {
      approximate = new Approximate(null, companyInfoRequestDto.country().toString(), null, null);
    }

    return new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,
        new WebSearchOptions.UserLocation("location", approximate));
  }

}
