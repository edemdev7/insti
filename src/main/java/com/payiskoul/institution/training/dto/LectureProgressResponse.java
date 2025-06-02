package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse de progression d'une leçon")
public record LectureProgressResponse(
        @Schema(description = "ID de la progression")
        String id,

        @Schema(description = "ID de l'inscription")
        String enrollmentId,

        @Schema(description = "ID de la leçon")
        String