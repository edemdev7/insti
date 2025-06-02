package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Requête d'inscription à une offre professionnelle
 */
@Schema(description = "Demande d'inscription à une offre professionnelle")
public record ProfessionalEnrollmentRequest(
        @Parameter(description = "ID de l'étudiant", required = true)
        @Schema(description = "ID de l'étudiant", example = "student-123")
        String studentId,

        @Parameter(description = "ID de l'offre professionnelle", required = true)
        @Schema(description = "ID de l'offre professionnelle", example = "prof-offer-001")
        String offerId
) {}