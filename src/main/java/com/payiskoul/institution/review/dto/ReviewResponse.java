package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Réponse d'évaluation")
public record ReviewResponse(
        @Schema(description = "ID de l'évaluation")
        String id,

        @Schema(description = "Informations sur l'étudiant")
        StudentInfo student,

        @Schema(description = "Note sur 5")
        Integer rating,

        @Schema(description = "Commentaire")
        String comment,

        @Schema(description = "Recommande l'offre")
        Boolean recommended,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
