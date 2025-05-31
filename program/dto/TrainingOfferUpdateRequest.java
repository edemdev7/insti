package com.payiskoul.institution.program.dto;

import com.payiskoul.institution.program.model.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

// ============ UPDATE REQUEST ============
@Schema(description = "Demande de mise à jour d'une offre de formation")
public record TrainingOfferUpdateRequest(
        @Schema(description = "Libellé de l'offre", example = "Licence 1 Informatique")
        String label,

        @Schema(description = "Type d'offre", example = "ACADEMIC")
        OfferType offerType,

        @Schema(description = "Description de l'offre", example = "Formation de base pour la licence 1")
        String description,

        @Min(value = 1, message = "La durée doit être au minimum égale à 1")
        @Schema(description = "Durée de la formation", example = "1")
        Integer duration,

        @Schema(description = "Unité de durée", example = "YEAR")
        DurationUnit durationUnit,

        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
        @Schema(description = "Montant des frais", example = "180000")
        BigDecimal tuitionAmount,

        @Schema(description = "Type de certification", example = "Licence")
        String certification,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear
) {}
