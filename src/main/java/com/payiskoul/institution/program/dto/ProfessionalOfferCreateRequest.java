package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;


// ============ PROFESSIONAL OFFER CREATE REQUEST ============
@Schema(description = "Demande de création d'une offre professionnelle")
public record ProfessionalOfferCreateRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Schema(description = "Titre de l'offre", example = "Formation DevOps Avancée")
        String title,

        @Schema(description = "Sous-titre", example = "Maîtrisez les outils DevOps modernes")
        String subtitle,

        @NotBlank(message = "La description est obligatoire")
        @Schema(description = "Description détaillée")
        String description,

        @Schema(description = "Image de couverture")
        String coverImage,

        @Schema(description = "Vidéo promotionnelle")
        String promotionalVideo,

        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.0", message = "Le montant doit être positif")
        @Schema(description = "Prix de la formation", example = "500000")
        BigDecimal price,

        @Min(value = 1, message = "La durée doit être au minimum égale à 1")
        @Schema(description = "Durée en heures", example = "40")
        int durationHours,

        @Schema(description = "Langue", example = "Français")
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

        @Schema(description = "Type de certification", example = "Certificat DevOps")
        String certification
) {}