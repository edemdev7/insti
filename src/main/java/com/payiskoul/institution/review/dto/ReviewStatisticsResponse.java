package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

// ReviewStatisticsResponse.java
@Schema(description = "Statistiques des avis")
public record ReviewStatisticsResponse(
        @Schema(description = "Note moyenne")
        Double averageRating,

        @Schema(description = "Nombre total d'avis")
        Integer totalReviews,

        @Schema(description = "Distribution des notes")
        Map<Integer, Integer> ratingDistribution,

        @Schema(description = "Pourcentage de recommandations")
        Double recommendationPercentage
) {}
