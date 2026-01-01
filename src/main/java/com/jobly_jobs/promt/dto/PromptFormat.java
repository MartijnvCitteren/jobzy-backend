package com.jobly_jobs.promt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptFormat {
    private String task;
    private String scope;
    private Limits limits;
    private Action action;
}
