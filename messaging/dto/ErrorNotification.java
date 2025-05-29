package com.payiskoul.institution.messaging.dto;

import lombok.Builder;
import java.time.LocalDateTime;

public record ErrorNotification(
        String source,
        String errorType,
        String message,
        String institutionId,
        LocalDateTime timestamp
) {
    @Builder
    public ErrorNotification {}
}