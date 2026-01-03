package com.jobly_jobs.domain.dto.response;

import lombok.Builder;

@Builder
public record ErrorDto(
        String status,
        String error,
        String message,
        String displayMessage

) {
}
