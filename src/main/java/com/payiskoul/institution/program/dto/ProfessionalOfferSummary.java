package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ============ PROFESSIONAL OFFER SUMMARY ============
@Schema(description = "Résumé d'une offre professionnelle")
public record ProfessionalOfferSummary(
        @Schema(description = "ID de l'offre")
        String id,

        @Schema(description = "Titre")
        String title,

        @Schema(description = "Image de couverture")
        String coverImage,

        @Schema(description = "Prix")
        BigDecimal price,

        @Schema(description = "Durée en heures")
        int durationHours,

        @Schema(description = "Langue")
        String language,

        @Schema(description = "Statut de publication")
        Boolean isPublished,

        @Schema(description = "Statut d'approbation")
        Boolean isApproved,

        @Schema(description = "Nombre d'étudiants")
        Integer totalStudents,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
