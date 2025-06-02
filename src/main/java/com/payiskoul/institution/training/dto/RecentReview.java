package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// RecentReview.java
@Schema(description = "Avis récent")
public record RecentReview(
        @Schema(description = "ID de l'avis")
        String id,

        @Schema(description = "Nom de l'étudiant")
        String studentName,

        @Schema(description = "Note")
        Integer rating,

        @Schema(description = "Commentaire")
        String comment,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
