package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

// ============ PROFESSIONAL OFFER UPDATE REQUEST ============
@Schema(description = "Demande de mise à jour d'une offre professionnelle")
public record ProfessionalOfferUpdateRequest(
        @Schema(description = "Titre de l'offre")
        String title,

        @Schema(description = "Sous-titre")
        String subtitle,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Image de couverture")
        String coverImage,

        @Schema(description = "Vidéo promotionnelle")
        String promotionalVideo,

        @DecimalMin(value = "0.0", message = "Le montant doit être positif")
        @Schema(description = "Prix")
        BigDecimal price,

        @Min(value = 1, message = "La durée doit être au minimum égale à 1")
        @Schema(description = "Durée en heures")
        Integer durationHours,

        @Schema(description = "Langue")
        String language,

        @Schema(description = "Prérequis")
        String prerequisites,

        @Schema(description = "Objectifs d'apprentissage")
        String learningObjectives,

        @Schema(description = "Public cible")
        String targetAudience,

        @Schema(description = "Modalités d'évaluation")
        String assessmentMethods,

        @Schema(description = "Ressources incluses")
        String includedResources,

        @Schema(description = "Certification")
        String certification
) {}

