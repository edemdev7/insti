package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Statistiques d'un quiz")
public record QuizStatisticsResponse(
        @Schema(description = "ID du quiz")
        String quizId,

        @Schema(description = "Nombre total de tentatives")
        Integer totalAttempts,

        @Schema(description = "Score moyen")
        Double averageScore,

        @Schema(description = "Taux de réussite (%)")
        Double passRate,

        @Schema(description = "Nombre d'étudiants uniques")
        Integer uniqueStudents,

        @Schema(description = "Nombre d'étudiants ayant réussi")
        Integer passedStudents,

        @Schema(description = "Distribution des scores")
        Map<String, Integer> scoreDistribution
) {}