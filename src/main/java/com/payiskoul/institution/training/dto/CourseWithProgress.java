package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// CourseWithProgress.java
@Schema(description = "Cours avec progression")
public record CourseWithProgress(
        @Schema(description = "ID de l'offre")
        String offerId,

        @Schema(description = "Titre de l'offre")
        String title,

        @Schema(description = "Progression en %")
        Double progress,

        @Schema(description = "Dernière consultation")
        LocalDateTime lastAccessed
) {}
