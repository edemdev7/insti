package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// ============ LIST RESPONSE ============
@Schema(description = "Réponse paginée contenant une liste d'offres")
public record TrainingOfferListResponse(
        @Schema(description = "Numéro de page", example = "0")
        int page,

        @Schema(description = "Taille de la page", example = "10")
        int size,

        @Schema(description = "Nombre total d'éléments", example = "25")
        long totalElements,

        @Schema(description = "Nombre total de pages", example = "3")
        int totalPages,

        @Schema(description = "Liste des offres")
        List<TrainingOfferSummary> offers
) {}
