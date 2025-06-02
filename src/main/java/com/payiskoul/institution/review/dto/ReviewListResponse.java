package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// ReviewListResponse.java
@Schema(description = "Liste paginée des avis")
public record ReviewListResponse(
        @Schema(description = "Numéro de page")
        int page,

        @Schema(description = "Taille de la page")
        int size,

        @Schema(description = "Nombre total d'éléments")
        long totalElements,

        @Schema(description = "Nombre total de pages")
        int totalPages,

        @Schema(description = "Liste des avis")
        List<ReviewResponse> reviews
) {}
