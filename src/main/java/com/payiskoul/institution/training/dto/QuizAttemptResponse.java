package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Résultat d'une tentative de quiz")
public record QuizAttemptResponse(
        @Schema(description = "ID de la tentative")
        String id,

        @Schema(description = "ID du quiz")
        String quizId,

        @Schema(description = "ID de l'inscription")
        String enrollmentId,

        @Schema(description = "Score obtenu")
        Double score,

        @Schema(description = "Score en pourcentage")
        Double percentage,

        @Schema(description = "Quiz réussi")
        Boolean passed,

        @Schema(description = "Réponses données")
        Map<Integer, List<String>> answers,

        @Schema(description = "Réponses correctes")
        Map<Integer, List<String>> correctAnswers,

        @Schema(description = "Temps passé en secondes")
        Integer timeSpent,

        @Schema(description = "Tentative numéro")
        Integer attemptNumber,

        @Schema(description = "Date de soumission")
        LocalDateTime submittedAt
) {}