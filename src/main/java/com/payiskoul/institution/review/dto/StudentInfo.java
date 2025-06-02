package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations sur l'étudiant")
public record StudentInfo(
        @Schema(description = "ID de l'étudiant")
        String id,

        @Schema(description = "Nom complet")
        String fullName,

        @Schema(description = "Matricule")
        String matricule
) {}
