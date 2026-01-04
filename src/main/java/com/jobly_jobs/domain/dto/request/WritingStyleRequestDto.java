package com.jobly_jobs.domain.dto.request;

import com.jobly_jobs.domain.enums.Language;
import com.jobly_jobs.domain.enums.WritingStyle;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record WritingStyleRequestDto(
    @NotBlank
    WritingStyle writingStyle,
    @NotBlank
    Language language
) {

}
