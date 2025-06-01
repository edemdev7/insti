package com.payiskoul.institution.tuition.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;
/**
 * Événement émis par le service Institutions en cas d'échec de traitement d'un paiement
 */
@Getter
@Setter
@Builder
public class TuitionPaymentFailedEvent {
    private String eventType;
    private String eventId;
    private UUID transactionId;
    private String studentMatricule;
    private String institutionId;
    private String reason;
    private OffsetDateTime timestamp;
}