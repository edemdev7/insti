package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



// ============ ENROLLMENT REQUEST ============
@Schema(description = "Demande d'inscription d'un étudiant à une offre")
public record OfferEnrollmentRequest(
        @Valid
        @NotNull(message = "Les informations de l'étudiant sont obligatoires")
        @Schema(description = "Informations de l'étudiant")
        StudentInfo student,

        @Valid
        @NotNull(message = "Les informations de l'offre sont obligatoires")
        @Schema(description = "Informations de l'offre")
        OfferInfo offer,

        @NotBlank(message = "L'ID de l'institution est obligatoire")
        @Schema(description = "ID de l'institution", example = "664f82a9e9d034c2fcb9b0e2")
        String institutionId,

        @Schema(description = "ID de la classe (optionnel)", example = "664f82a9e9d034c2feca9b0e2")
        String classroomId
) {}

// ============ STUDENT INFO ============
@Schema(description = "Informations sur un étudiant")
public record StudentInfo(
        @Schema(description = "ID de l'étudiant", example = "634f82a9e9d034c2fca9b0e2")
        String id,

        @NotBlank(message = "Le nom complet est obligatoire")
        @Schema(description = "Nom complet de l'étudiant", example = "Assi Mason")
        String fullname
) {}

// ============ OFFER INFO ============
@Schema(description = "Informations sur une offre")
public record OfferInfo(
        @NotBlank(message = "L'ID de l'offre est obligatoire")
        @Schema(description = "ID de l'offre", example = "664f82a9e9d034f2fca9b0e2")
        String id,

        @NotBlank(message = "Le titre de l'offre est obligatoire")
        @Schema(description = "Titre de l'offre", example = "Master 1")
        String title
) {}



