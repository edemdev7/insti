package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tableau de bord de l'institution")
public record InstitutionDashboardResponse(
        @Schema(description = "Nombre total d'offres")
        Integer totalOffers,

        @Schema(description = "Offres publiées")
        Integer publishedOffers,

        @Schema(description = "Offres approuvées")
        Integer approvedOffers,

        @Schema(description = "Nombre total d'étudiants")
        Integer totalStudents,

        @Schema(description = "Inscriptions récentes")
        List<RecentEnrollment> recentEnrollments,

        @Schema(description = "Avis récents")
        List<RecentReview> recentReviews,

        @Schema(description = "Revenus du mois")
        java.math.BigDecimal monthlyRevenue
) {}