package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
/**
 * Représente un enregistrement d'importation en échec
 */
@Schema(description = "Enregistrement d'importation en échec")
public record FailedImportRecord(
        @Schema(description = "Données de l'étudiant en échec")
        Map<String, String> studentData,

        @Schema(description = "Message d'erreur", example = "Email invalide")
        String errorMessage
) {
}