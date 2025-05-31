package com.payiskoul.institution.program.dto;

import com.payiskoul.institution.program.model.OfferType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

// ============ RESPONSE ============
@Schema(description = "Réponse contenant les informations d'une offre de formation")
public record TrainingOfferResponse(
        @Schema(description = "ID de l'offre", example = "offer-001")
        String id,

        @Schema(description = "Libellé de l'offre", example = "Licence 1 Informatique")
        String label,

        @Schema(description = "Type d'offre", example = "ACADEMIC")
        OfferType offerType,

        @Schema(description = "Code généré", example = "LIC1-ESATIC-2025")
        String code,

        @Schema(description = "Description de l'offre")
        String description,

        @Schema(description = "Durée", example = "1")
        int duration,

        @Schema(description = "Unité de durée", example = "YEAR")
        DurationUnit durationUnit,

        @Schema(description = "Montant des frais", example = "180000")
        BigDecimal tuitionAmount,

        @Schema(description = "Devise", example = "XOF")
        String currency,

        @Schema(description = "Certification", example = "Licence")
        String certification,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear,

        @Schema(description = "Information sur l'institution")
        InstitutionInfo institution,

        @Schema(description = "Nombre total d'étudiants inscrits", example = "10")
        Integer totalStudents,

        @Schema(description = "Liste des classes (uniquement pour ACADEMIC)")
        List<ClassroomInfo> classrooms
) {}
