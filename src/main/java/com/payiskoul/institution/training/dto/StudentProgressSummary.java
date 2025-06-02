package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// StudentProgressSummary.java
@Schema(description = "Résumé de progression de l'étudiant")
public record StudentProgressSummary(
        @Schema(description = "Progression globale en %")
        Double overallProgress,

        @Schema(description = "Leçons terminées")
        Integer completedLectures,

        @Schema(description = "Total de leçons")
        Integer totalLectures,

        @Schema(description = "Dernière leçon consultée")
        String lastLectureId
) {}
