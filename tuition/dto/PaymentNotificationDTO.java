package com.payiskoul.institution.tuition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Builder
@Schema(description = "Notification de paiement reçue du système de paiement")
public record PaymentNotificationDTO(
        @Schema(description = "Matricule de l'étudiant", example = "PI-CI-25A0123")
        String matricule,

        @Schema(description = "Montant du paiement", example = "50000")
        BigDecimal amount,

        @Schema(description = "Devise", example = "XOF")
        String currency,

        @Schema(description = "Référence unique de la transaction", example = "TRX987456321")
        String reference,

        @Schema(description = "ID de l'inscription concernée", example = "68076d9cc2fd0f315af8e709")
        String enrollmentId,

        @Schema(description = "ID du compte de l'institution", example = "8f402d79-d799-46bc-ba99-2748af975886")
        String institutionAccountId,

        @Schema(description = "Date du paiement", example = "2025-04-17T15:12:00Z")
        LocalDateTime paymentDate

) {}