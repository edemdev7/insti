package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// ============ OFFER QUERY PARAMS ============
@Schema(description = "Paramètres de recherche pour les offres")
public record OfferQueryParams(
        @Schema(description = "Recherche dans le titre")
        String title,

        @Schema(description = "Langue")
        String language,

        @Schema(description = "Prix minimum")
        BigDecimal minPrice,

        @Schema(description = "Prix maximum")
        BigDecimal maxPrice,

        @Schema(description = "Statut de publication")
        Boolean isPublished,

        @Schema(description = "Statut d'approbation")
        Boolean isApproved,

        @Schema(description = "Numéro de page")
        Integer page,

        @Schema(description = "Taille de la page")
        Integer size
) {}
