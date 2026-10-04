package app.jobzy.api.adapter.out.ai.support.prompt;

import app.jobzy.api.adapter.out.ai.PromptBase;
import app.jobzy.api.adapter.out.ai.vacancy.VacancyDescriptionPromptInfo;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;

@Slf4j
@NoArgsConstructor
public class PromptFactory {
  private static final PromptCreator vacancyDescriptionPromptCreator =
      new VacancyDescriptionPrompt();

  public static Prompt createPrompt(PromptBase promptInput) {
    return switch (promptInput) {
      case VacancyDescriptionPromptInfo input -> vacancyDescriptionPromptCreator.create(input);
      default ->
          throw new IllegalArgumentException(
              "Unexpected prompt input type. type is: " + promptInput.getClass().getSimpleName());
    };
  }
}
