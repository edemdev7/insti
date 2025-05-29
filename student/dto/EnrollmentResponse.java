package com.payiskoul.institution.student.dto;


import com.payiskoul.institution.student.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(description = "Réponse pour une inscription à un programme")
public record EnrollmentResponse(
        @Schema(description = "ID unique de l'inscription", example = "66101243b3f2e6745e0d1ab7")
        String id,

        @Schema(description = "ID de l'étudiant", example = "660fa3dfedc9f62233a9db77")
        String studentId,

        @Schema(description = "ID du niveau/programme", example = "660fb32ccf403c04dbd37ee4")
        String levelId,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear,

        @Schema(description = "Statut de l'inscription", example = "ENROLLED")
        Enrollment.EnrollmentStatus status,

        @Schema(description = "Date d'inscription", example = "2025-04-17T10:15:30Z")
        LocalDateTime registeredAt
) {
}