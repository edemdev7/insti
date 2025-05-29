package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProgramLevelResponse(
        @Schema(description = "Identifiant unique du niveau", example = "354a87..")
        String id,

        @Schema(description = "Identifiant de l'institution associée", example = "654e81...")
        String institutionId,

        @Schema(description = "Code unique du niveau", example = "LIC2-ISMA-2025")
        String code,

        @Schema(description = "Nom du niveau", example = "Licence 2")
        String name,

        @Schema(description = "Année académique", example = "2025-2026")
        String academicYear,

        @Schema(description = "Montant des frais de scolarité", example = "250000")
        BigDecimal tuitionAmount,

        @Schema(description = "Devise des frais de scolarité", example = "XOF")
        String currency,

        @Schema(description = "Durée du programme", example = "1")
        int duration,

        @Schema(description = "Unité de temps pour la durée", example = "Year")
        DurationUnit durationUnit,

        @Schema(description = "Type de certification obtenue", example = "Licence")
        String certification
) {}