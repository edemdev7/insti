package com.payiskoul.institution.student.dto;

import com.payiskoul.institution.tuition.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// ============ STUDENT OFFER INFO ============
@Schema(description = "Informations d'un étudiant dans une offre")
public record StudentOfferInfo(
        @Schema(description = "ID de l'étudiant", example = "664f82a9e9d034f2fca9b0e2")
        String id,

        @Schema(description = "Matricule de l'étudiant", example = "PI-CI-25A0012")
        String matricule,

        @Schema(description = "Nom complet", example = "Ahou Rebecca")
        String fullName,

        @Schema(description = "Email", example = "rebecca@example.com")
        String email,

        @Schema(description = "Informations sur la classe assignée")
        ClassroomInfo classroom,

        @Schema(description = "Statut de paiement", example = "PARTIALLY_PAID")
        PaymentStatus paymentStatus,

        @Schema(description = "Montant payé", example = "50000")
        BigDecimal amountPaid,

        @Schema(description = "Montant restant", example = "130000")
        BigDecimal amountRemaining
) {}
