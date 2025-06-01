// com.payiskoul.institution.tuition.dto.TuitionPaymentEvent.java
package com.payiskoul.institution.tuition.dto;

import com.payiskoul.institution.tuition.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Événement de paiement de frais de scolarité")
public record TuitionPaymentEvent(
        @Schema(description = "ID de l'événement", example = "6632138e5a875b301f9d402a")
        String id,

        @Schema(description = "Matricule de l'étudiant", example = "PI-CI-25A0123")
        String matricule,

        @Schema(description = "Nom complet de l'étudiant", example = "Kouamé Aya")
        String studentName,

        @Schema(description = "ID de l'inscription", example = "68076d9cc2fd0f315af8e709")
        String enrollmentId,
        @Schema(description = "ID du compte de l'institution", example = "8f402d79-d799-46bc-ba99-2748af975886")
        String accountId,

        @Schema(description = "Montant du paiement", example = "50000")
        BigDecimal paymentAmount,

        @Schema(description = "Montant total des frais", example = "250000")
        BigDecimal totalAmount,

        @Schema(description = "Montant total payé", example = "150000")
        BigDecimal totalPaid,

        @Schema(description = "Montant restant à payer", example = "100000")
        BigDecimal remainingAmount,

        @Schema(description = "Devise", example = "XOF")
        String currency,

        @Schema(description = "Statut du paiement", example = "PARTIALLY_PAID")
        PaymentStatus paymentStatus,

        @Schema(description = "Référence de la transaction", example = "TRX987456321")
        String reference,

        @Schema(description = "Date du paiement", example = "2025-04-17T15:12:00Z")
        LocalDateTime paymentDate,

        @Schema(description = "Date de traitement", example = "2025-04-17T15:12:05Z")
        LocalDateTime processedAt
) {}