package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ============ QUERY PARAMS ============
@Schema(description = "Paramètres de recherche pour les offres")
public record TrainingOfferQueryParams(
        @Schema(description = "Type d'offre", example = "ACADEMIC")
        String type,

        @Schema(description = "Libellé de l'offre", example = "licence")
        String label,

        @Schema(description = "Code de l'offre", example = "L1")
        String code,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear,

        @Schema(description = "Numéro de page", example = "0")
        Integer page,

        @Schema(description = "Taille de la page", example = "10")
        Integer size
) {}
