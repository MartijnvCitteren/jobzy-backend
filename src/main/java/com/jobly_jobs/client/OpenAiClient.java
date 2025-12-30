package com.jobly_jobs.client;

import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions.UserLocation.Approximate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;


@Service
@Log4j2
public class OpenAiClient {
    private final ChatClient chatClient;

    public OpenAiClient(ChatClient chatClient) {
        log.debug("OpenAiClient created");
        this.chatClient = chatClient;
    }

    public GeneratedVacancyDto getResponse(String message) {
        try {
            return chatClient.prompt().user(message).call().entity(GeneratedVacancyDto.class);
        } catch (RestClientException e) {
            log.error("Error while calling OpenAI API", e);
            throw e;
        }
    }

    public AiCompanyInfo getCompanyInfo() {
        Approximate approximate = new Approximate(null, "The Netherlands", null, null);
        WebSearchOptions webSearchOptions = new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,
                                                                 new WebSearchOptions.UserLocation("location", approximate));

        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setWebSearchOptions(webSearchOptions);

        String message = """
                         research about the company Coolblue, check especially the website 'www.coolblue.nl'.
                         Summarize it in 3 lines, write their goal down in max 2 lines, 
                         write down what the USP's are for employees who work there.
                         Format your response acording to the provided entity
                         """;

        return chatClient.prompt().options(openAiChatOptions).user(message).call().entity(AiCompanyInfo.class);
    }


}
