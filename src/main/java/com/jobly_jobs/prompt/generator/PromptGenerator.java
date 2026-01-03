package com.jobly_jobs.prompt.generator;

import com.jobly_jobs.prompt.dto.PromptFormat;

public interface PromptGenerator<T> {
    PromptFormat getPrompt(T t);

}
