package com.payiskoul.institution.tuition.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Événement émis par le service Institutions pour confirmer le traitement d'un paiement
 */
@Getter
@Setter
@Builder
public class TuitionPaymentConfirmedEvent {
    private String eventType;
    private String eventId;
    private UUID transactionId;
    private String studentMatricule;
    private BigDecimal amountPaid;
    private String newStatus;
    private BigDecimal remainingAmount;
    private String institutionId;
    private UUID accountId;
    private OffsetDateTime timestamp;
}
