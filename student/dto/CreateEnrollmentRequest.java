package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Données pour l'inscription d'un étudiant à un programme")
public record CreateEnrollmentRequest(
        @Schema(description = "ID de l'étudiant", example = "660fa3dfedc9f62233a9db77")
        @NotBlank(message = "L'ID de l'étudiant est obligatoire")
        String studentId,

        @Schema(description = "ID du programme", example = "660fb32ccf403c04dbd37ee4")
        @NotBlank(message = "L'ID du programme est obligatoire")
        String programId,

        @Schema(description = "Année académique", example = "2024-2025")
        @NotBlank(message = "L'année académique est obligatoire")
        @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "L'année académique doit être au format YYYY-YYYY")
        String academicYear,

        @Schema(description = "ID de la classe à laquelle l'étudiant sera assigné (optionnel)", example = "660fb32ccf403c04dbd37ee5")
        String classroomId
) {
}