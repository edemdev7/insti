package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// OfferStatisticsResponse.java
@Schema(description = "Statistiques d'une offre")
public record OfferStatisticsResponse(
        @Schema(description = "Nombre total d'étudiants")
        Integer totalStudents,

        @Schema(description = "Note moyenne")
        Double averageRating,

        @Schema(description = "Nombre total d'avis")
        Integer totalReviews,

        @Schema(description = "Taux de complétion moyen")
        Double completionRate,

        @Schema(description = "Revenus générés")
        java.math.BigDecimal totalRevenue
) {}
