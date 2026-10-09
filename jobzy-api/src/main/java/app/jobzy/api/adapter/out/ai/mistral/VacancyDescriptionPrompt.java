package app.jobzy.api.adapter.out.ai.mistral;

import app.jobzy.api.domain.generation.valueobject.GenerationInput;
import app.jobzy.api.domain.vacancy.valueobject.Language;
import java.util.Map;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;

/**
 * The versioned prompt for drafting a vacancy description, rendered from the classpath templates
 * under {@code prompt/vacancy-description/<version>/}. A prompt change is a new version directory
 * in a reviewed PR, never a runtime setting, and every generation records the version it used.
 *
 * <p>The user's answers go into the user message only, each wrapped in fixed delimiters that the
 * system prompt declares to be data. The delimiter brackets are stripped from the answers first, so
 * an answer cannot close its own block and pose as an instruction.
 */
class VacancyDescriptionPrompt {

  static final String VERSION = "vacancy-description/v1";

  private static final String DELIMITER_OPEN = "[[[";
  private static final String DELIMITER_CLOSE = "]]]";

  private final PromptTemplate systemTemplate;
  private final PromptTemplate userTemplate;

  VacancyDescriptionPrompt() {
    this.systemTemplate = load("system.st");
    this.userTemplate = load("user.st");
  }

  /**
   * @param canary a token unique to this call; seeing it in the output means the model leaked its
   *     instructions
   */
  String system(Language language, String canary) {
    return systemTemplate.render(Map.of("language", languageName(language), "canary", canary));
  }

  String user(GenerationInput input) {
    return userTemplate.render(
        Map.of(
            "jobTitle", stripDelimiters(input.jobTitle()),
            "mostImportantTasks", stripDelimiters(input.mostImportantTasks()),
            "team", stripDelimiters(input.team()),
            "whyNiceJob", stripDelimiters(input.whyNiceJob())));
  }

  static String stripDelimiters(String text) {
    String stripped = text;
    // Repeat until stable: removing "[[[" from "[[[[[[" leaves "[[[" again.
    while (stripped.contains(DELIMITER_OPEN) || stripped.contains(DELIMITER_CLOSE)) {
      stripped = stripped.replace(DELIMITER_OPEN, "").replace(DELIMITER_CLOSE, "");
    }
    return stripped;
  }

  private static String languageName(Language language) {
    return switch (language) {
      case NL -> "Dutch";
      case EN -> "English";
      case DE -> "German";
      case FR -> "French";
    };
  }

  private static PromptTemplate load(String fileName) {
    var resource = new ClassPathResource("prompt/" + VERSION + "/" + fileName);
    return PromptTemplate.builder().resource(resource).build();
  }
}
