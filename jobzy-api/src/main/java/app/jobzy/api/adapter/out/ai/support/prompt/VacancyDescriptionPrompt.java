package app.jobzy.api.adapter.out.ai.support.prompt;

import app.jobzy.api.adapter.out.ai.PromptBase;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class VacancyDescriptionPrompt implements PromptCreator {

  @Value("classpath:prompt/vacancy/VacancySystemPrompt.txt")
  private Resource systemPrompt;

  @Value("classpath:prompt/vacancy/VacancyDescriptionPrompt.txt")
  private Resource vacancyDescriptionPromptTemplate;

  @Override
  public Prompt create(PromptBase promptInput) {

    var template = new PromptTemplate(vacancyDescriptionPromptTemplate);

    return null;
  }
}
