package com.jobly_jobs.client;

import com.jobly_jobs.domain.dto.AiCompanyInfo;
import com.jobly_jobs.domain.dto.request.CompanyInfoRequestDto;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import com.jobly_jobs.promt.dto.PromptFormat;
import dev.toonformat.jtoon.JToon;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.WebSearchOptions.UserLocation.Approximate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;


@Service
@Log4j2
public class OpenAiClient implements AiClient {
    private final ChatClient chatClient;

    public OpenAiClient(ChatClient chatClient) {
        log.debug("OpenAiClient created");
        this.chatClient = chatClient;
    }

    @Override
    public GeneratedVacancyDto getResponse(String message) {
        try {
            return chatClient.prompt().user(message).call().entity(GeneratedVacancyDto.class);
        } catch (RestClientException e) {
            log.error("Error while calling OpenAI API", e);
            throw e;
        }
    }

    @Override
    public AiCompanyInfo getCompanyInfo(PromptFormat prompt,  CompanyInfoRequestDto companyInfoRequestDto) {
        WebSearchOptions webSearchOptions = createWebSearchOptions(companyInfoRequestDto);
        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setWebSearchOptions(webSearchOptions);

        String toonPrompt = JToon.encode(prompt);


        return chatClient.prompt().options(openAiChatOptions).user(toonPrompt).call().entity(AiCompanyInfo.class);
    }

    private WebSearchOptions createWebSearchOptions(CompanyInfoRequestDto companyInfoRequestDto) {
        Approximate approximate;
        if(ObjectUtils.isEmpty(companyInfoRequestDto.country())){
            approximate = null;
        } else {
            approximate = new Approximate(null, companyInfoRequestDto.country().toString(), null, null);
        }

        return new WebSearchOptions(WebSearchOptions.SearchContextSize.LOW,
                                                                 new WebSearchOptions.UserLocation("location", approximate));

    }


}
