package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// ============ PROFESSIONAL OFFER LIST RESPONSE ============
@Schema(description = "Réponse paginée pour les offres professionnelles")
public record ProfessionalOfferListResponse(
        @Schema(description = "Numéro de page")
        int page,

        @Schema(description = "Taille de la page")
        int size,

        @Schema(description = "Nombre total d'éléments")
        long totalElements,

        @Schema(description = "Nombre total de pages")
        int totalPages,

        @Schema(description = "Liste des offres")
        List<ProfessionalOfferSummary> offers
) {}
