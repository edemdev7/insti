package com.payiskoul.institution.student.dto;

import com.payiskoul.institution.student.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// ============ STATUS UPDATE RESPONSE ============
@Schema(description = "Réponse de mise à jour du statut d'inscription")
public record EnrollmentStatusUpdateResponse(
        @Schema(description = "ID de l'inscription", example = "664f82a9e9d034f2fca9b0e1")
        String id,

        @Schema(description = "Nouveau statut", example = "COMPLETED")
        Enrollment.EnrollmentStatus status,

        @Schema(description = "Date de mise à jour", example = "2024-05-25T16:40:00Z")
        LocalDateTime updatedAt
) {}
