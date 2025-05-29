package com.payiskoul.institution.tuition.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse contenant les informations de suivi des paiements d'un étudiant")
public record TuitionResponse(
        @Schema(description = "Matricule de l'étudiant", example = "PI-CI-25A0123")
        String matricule,

        @Schema(description = "Nom complet de l'étudiant", example = "Jean Kouadio")
        String fullName,

        @Schema(description = "Liste des frais de scolarité")
        TuitionInfo[] tuitions
) {
}