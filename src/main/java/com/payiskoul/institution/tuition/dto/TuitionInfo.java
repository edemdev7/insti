package com.payiskoul.institution.tuition.dto;

import com.payiskoul.institution.tuition.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Informations sur les frais de scolarité d'un programme")
public record TuitionInfo(
        @Schema(description = "Informations sur le programme")
        ProgramInfo program,

        @Schema(description = "Montant total des frais de scolarité", example = "250000")
        BigDecimal totalAmount,

        @Schema(description = "Devise", example = "XOF")
        String currency,

        @Schema(description = "Montant payé", example = "100000")
        BigDecimal paidAmount,

        @Schema(description = "Montant restant à payer", example = "150000")
        BigDecimal remainingAmount,

        @Schema(description = "Statut du paiement", example = "PARTIALLY_PAID")
        PaymentStatus paymentStatus
) {
}