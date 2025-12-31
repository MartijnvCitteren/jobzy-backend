package com.jobly_jobs.promt;

import com.jobly_jobs.promt.dto.PromptFormat;

public interface PromtGenerator<T> {
    PromptFormat getPrompt(T t);

}
