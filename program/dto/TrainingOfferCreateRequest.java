package com.payiskoul.institution.program.dto;

import com.payiskoul.institution.program.model.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

// ============ CREATE REQUEST ============
@Schema(description = "Demande de création d'une offre de formation")
public record TrainingOfferCreateRequest(
        @NotBlank(message = "Le libellé est obligatoire")
        @Schema(description = "Libellé de l'offre", example = "Licence 1 Informatique")
        String label,

        @NotNull(message = "Le type d'offre est obligatoire")
        @Schema(description = "Type d'offre", example = "ACADEMIC")
        OfferType offerType,

        @Schema(description = "Description de l'offre", example = "Formation de base pour la licence 1")
        String description,

        @Min(value = 1, message = "La durée doit être au minimum égale à 1")
        @Schema(description = "Durée de la formation", example = "1")
        int duration,

        @NotNull(message = "L'unité de durée est obligatoire")
        @Schema(description = "Unité de durée", example = "YEAR")
        DurationUnit durationUnit,

        @NotNull(message = "Le montant des frais est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
        @Schema(description = "Montant des frais", example = "180000")
        BigDecimal tuitionAmount,

        @Schema(description = "Type de certification", example = "Licence")
        String certification,

        @NotBlank(message = "L'année académique est obligatoire")
        @Schema(description = "Année académique (YYYY-YYYY pour ACADEMIC, YYYY pour PROFESSIONAL)",
                example = "2024-2025")
        String academicYear
) {}

// ============ SUPPORTING RECORDS ============
@Schema(description = "Information sur une institution")
public record InstitutionInfo(
        @Schema(description = "ID de l'institution", example = "664f82a9e9d034c2fca9b0e2")
        String id,

        @Schema(description = "Nom de l'institution", example = "ESATIC")
        String name
) {}

@Schema(description = "Information sur une classe")
public record ClassroomInfo(
        @Schema(description = "ID de la classe", example = "664f82a9e9d034c2fca9b0e8")
        String id,

        @Schema(description = "Nom de la classe", example = "Licence 1 A")
        String name,

        @Schema(description = "Nombre d'étudiants actuels", example = "10")
        int currentCount,

        @Schema(description = "Capacité maximale", example = "30")
        int capacity
) {}