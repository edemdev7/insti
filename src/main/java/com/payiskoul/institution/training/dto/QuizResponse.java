package com.payiskoul.institution.training.dto;

import com.payiskoul.institution.training.model.QuizQuestion;
import com.payiskoul.institution.training.model.TrainingQuiz;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Réponse contenant les informations d'un quiz")
public record QuizResponse(
        @Schema(description = "ID du quiz")
        String id,

        @Schema(description = "Titre du quiz")
        String title,

        @Schema(description = "Description du quiz")
        String description,

        @Schema(description = "Type de parent (SECTION ou LECTURE)")
        TrainingQuiz.ParentType parentType,

        @Schema(description = "ID du parent (section ou lecture)")
        String parentId,

        @Schema(description = "Questions du quiz")
        List<QuizQuestion> questions,

        @Schema(description = "Score minimum pour réussir")
        Integer passingScore,

        @Schema(description = "Durée limite en minutes")
        Integer timeLimit,

        @Schema(description = "Nombre maximum de tentatives")
        Integer maxAttempts,

        @Schema(description = "Questions mélangées")
        Boolean shuffleQuestions,

        @Schema(description = "Réponses affichées immédiatement")
        Boolean showAnswersImmediately,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}