package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "Demande de soumission d'une tentative de quiz")
public record QuizAttemptRequest(
        @NotNull(message = "L'ID du quiz est obligatoire")
        @Schema(description = "ID du quiz", example = "quiz-123")
        String quizId,

        @NotNull(message = "L'ID de l'inscription est obligatoire")
        @Schema(description = "ID de l'inscription", example = "enrollment-456")
        String enrollmentId,

        @NotEmpty(message = "Les réponses sont obligatoires")
        @Schema(description = "Réponses de l'étudiant - Map<questionIndex, selectedAnswers>")
        Map<Integer, List<String>> answers,

        @Schema(description = "Temps passé sur le quiz en secondes", example = "1800")
        Integer timeSpent
) {}