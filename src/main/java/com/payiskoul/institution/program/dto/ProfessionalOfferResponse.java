package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import java.math.BigDecimal;
import java.time.LocalDateTime;
// ============ PROFESSIONAL OFFER RESPONSE ============
@Schema(description = "Réponse contenant les informations d'une offre professionnelle")
public record ProfessionalOfferResponse(
        @Schema(description = "ID de l'offre", example = "offer-prof-001")
        String id,

        @Schema(description = "Titre de l'offre", example = "Formation DevOps Avancée")
        String title,

        @Schema(description = "Sous-titre")
        String subtitle,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Image de couverture")
        String coverImage,

        @Schema(description = "Vidéo promotionnelle")
        String promotionalVideo,

        @Schema(description = "Prix", example = "500000")
        BigDecimal price,

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

        @Schema(description = "Certification")
        String certification,

        @Schema(description = "Statut de publication")
        Boolean isPublished,

        @Schema(description = "Statut d'approbation")
        Boolean isApproved,

        @Schema(description = "Date d'approbation")
        LocalDateTime approvalDate,

        @Schema(description = "Information sur l'institution")
        InstitutionInfo institution,

        @Schema(description = "Nombre total d'étudiants inscrits")
        Integer totalStudents,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
