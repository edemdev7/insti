package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tableau de bord de l'étudiant")
public record StudentDashboardResponse(
        @Schema(description = "Nombre total d'inscriptions")
        Integer totalEnrollments,

        @Schema(description = "Cours récents avec progression")
        List<CourseWithProgress> recentCourses,

        @Schema(description = "Progression globale moyenne")
        Double overallProgress,

        @Schema(description = "Certificats obtenus")
        List<CertificateInfo> certificates
) {}
