package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// RecentEnrollment.java
@Schema(description = "Inscription récente")
public record RecentEnrollment(
        @Schema(description = "ID de l'inscription")
        String id,

        @Schema(description = "Nom de l'étudiant")
        String studentName,

        @Schema(description = "Nom de l'offre")
        String offerName,

        @Schema(description = "Date d'inscription")
        LocalDateTime enrolledAt
) {}
