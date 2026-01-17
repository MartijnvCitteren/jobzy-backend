package app.jobzy.domain.dto.request;

import app.jobzy.domain.enums.Language;
import app.jobzy.domain.enums.WritingStyle;
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
