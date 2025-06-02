package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Réponse de progression d'une leçon")
public record LectureProgressResponse(
        @Schema(description = "ID de la progression")
        String id,

        @Schema(description = "ID de l'inscription")
        String enrollmentId,

        @Schema(description = "ID de la leçon")
        String lectureId,

        @Schema(description = "Leçon terminée", example = "false")
        Boolean isCompleted,

        @Schema(description = "Pourcentage de progression", example = "75")
        Integer progressPercent,

        @Schema(description = "Position actuelle", example = "1250")
        Integer currentPosition,

        @Schema(description = "Temps passé en secondes", example = "3600")
        Integer timeSpent,

        @Schema(description = "Dernière consultation")
        LocalDateTime lastAccessedAt,

        @Schema(description = "Date de completion")
        LocalDateTime completedAt
) {}