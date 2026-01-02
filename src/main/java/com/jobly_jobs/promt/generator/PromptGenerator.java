package com.jobly_jobs.promt.generator;

import com.jobly_jobs.promt.dto.PromptFormat;

public interface PromptGenerator<T> {
    PromptFormat getPrompt(T t);

}
