package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Demande de mise à jour de progression")
public record UpdateProgressRequest(
        @Schema(description = "Pourcentage de progression (0-100)", example = "75")
        Integer progressPercent,

        @Schema(description = "Position actuelle en secondes", example = "1250")
        Integer currentPosition,

        @Schema(description = "Temps passé en secondes", example = "3600")
        Integer timeSpent,

        @Schema(description = "Leçon terminée", example = "false")
        Boolean isCompleted
) {}
