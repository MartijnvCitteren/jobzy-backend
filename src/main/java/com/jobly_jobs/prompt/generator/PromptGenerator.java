package com.jobly_jobs.prompt.generator;

import com.jobly_jobs.prompt.dto.PromptValues;

public interface PromptGenerator<T> {
  PromptValues<T> getPrompt(T t);

}
