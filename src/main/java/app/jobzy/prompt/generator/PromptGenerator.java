package app.jobzy.prompt.generator;

import app.jobzy.prompt.dto.PromptValues;

public interface PromptGenerator<T> {
  PromptValues<T> getPrompt(T t);

}
