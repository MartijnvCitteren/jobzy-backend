package com.jobly_jobs.domain.dto.request;

import com.jobly_jobs.domain.enums.Language;
import com.jobly_jobs.domain.enums.WritingStyle;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record WritingStyleRequestDto(
    @NotNull
    WritingStyle writingStyle,
    @NotNull
    Language language
) {

}
