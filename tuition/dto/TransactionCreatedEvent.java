package com.payiskoul.institution.tuition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Représente un événement de transaction créée
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private String eventType;
    private UUID eventId;
    private OffsetDateTime eventDate;
    private TransactionEventPayload payload;

    /**
     * Représente la charge utile d'un événement de transaction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionEventPayload {
        private UUID transactionId;
        private UUID accountId;
        private UUID receiverAccountId;
        private String transactionType;
        private String status;
        private String currencyCode;
        private OffsetDateTime transactionDate;
        private String description;
        private String reference;
        private String initiatorId;
        private String phoneNumber;
        private ChannelEventData channel;
        private String failureReason;
        private BigDecimal initialAmount;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private String feeResponsibility;
        private BigDecimal amountReceived;
        private String category;

    }

    /**
     * Représente les données du canal dans un événement
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelEventData {
        private String type;
        private String provider;
        private Long cardId;
        private String last4Digits;
        private String maskedCardNumber;
        private String mode;
    }
}