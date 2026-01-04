package com.jobly_jobs.service;

import com.jobly_jobs.client.OpenAiClient;
import com.jobly_jobs.domain.dto.response.GeneratedVacancyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class VacancyTextService {

  private final OpenAiClient openAiClient;

  public GeneratedVacancyDto generatedVacancyText(String prompt) {
    return generateText(prompt);
  }

  private GeneratedVacancyDto generateText(String prompt) {
    return openAiClient.getResponse(prompt);
  }


}
