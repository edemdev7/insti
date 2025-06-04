package com.payiskoul.institution.training.dto;

import com.payiskoul.institution.training.model.QuizQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Demande de création d'un quiz")
public record QuizCreateRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Schema(description = "Titre du quiz", example = "Quiz Docker - Chapitre 1")
        String title,

        @Schema(description = "Description du quiz")
        String description,

        @NotEmpty(message = "Le quiz doit contenir au moins une question")
        @Valid
        @Schema(description = "Liste des questions")
        List<QuizQuestion> questions,

        @Min(value = 0, message = "Le score minimum doit être positif")
        @Max(value = 100, message = "Le score minimum ne peut pas dépasser 100")
        @Schema(description = "Score minimum pour réussir (%)", example = "70")
        Integer passingScore,

        @Min(value = 1, message = "La durée doit être d'au moins 1 minute")
        @Schema(description = "Durée limite en minutes", example = "30")
        Integer timeLimit,

        @Min(value = 1, message = "Au moins une tentative doit être autorisée")
        @Schema(description = "Nombre maximum de tentatives", example = "3")
        Integer maxAttempts,

        @Schema(description = "Mélanger les questions", example = "true")
        Boolean shuffleQuestions,

        @Schema(description = "Afficher les réponses immédiatement", example = "false")
        Boolean showAnswersImmediately
) {}