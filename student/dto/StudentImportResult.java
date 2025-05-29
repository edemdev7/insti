package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * DTO pour les résultats d'importation d'étudiants
 */
@Schema(description = "Résultat de l'importation d'étudiants")
public record StudentImportResult(
        @Schema(description = "Nombre d'étudiants importés avec succès", example = "42")
        int successCount,

        @Schema(description = "Nombre d'étudiants en échec d'importation", example = "3")
        int failedCount,

        @Schema(description = "Liste des enregistrements en échec avec détails")
        List<FailedImportRecord> failedStudents
) {
}
