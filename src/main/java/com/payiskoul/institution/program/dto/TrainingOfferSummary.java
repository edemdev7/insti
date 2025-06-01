package com.payiskoul.institution.program.dto;

import com.payiskoul.institution.program.model.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// ============ OFFER SUMMARY ============
@Schema(description = "Résumé d'une offre de formation pour la liste")
public record TrainingOfferSummary(
        @Schema(description = "ID de l'offre", example = "offer-001")
        String id,

        @Schema(description = "Libellé de l'offre", example = "Licence 1 Informatique")
        String label,

        @Schema(description = "Type d'offre", example = "ACADEMIC")
        OfferType offerType,

        @Schema(description = "Code généré", example = "LIC1-ESATIC-2025")
        String code,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear,

        @Schema(description = "Montant des frais", example = "180000")
        BigDecimal tuitionAmount,

        @Schema(description = "Devise", example = "XOF")
        String currency
) {}
