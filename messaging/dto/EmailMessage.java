package com.payiskoul.institution.messaging.dto;

import lombok.Builder;

public record EmailMessage(
        String to,
        String subject,
        String message
) {
    @Builder
    public EmailMessage {}
}