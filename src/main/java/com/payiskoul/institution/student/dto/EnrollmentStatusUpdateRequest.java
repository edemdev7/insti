package com.payiskoul.institution.student.dto;

import com.payiskoul.institution.student.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// ============ STATUS UPDATE REQUEST ============
@Schema(description = "Demande de mise à jour du statut d'inscription")
public record EnrollmentStatusUpdateRequest(
        @NotNull(message = "Le statut est obligatoire")
        @Schema(description = "Nouveau statut", example = "COMPLETED",
                allowableValues = {"ENROLLED", "COMPLETED", "CANCELLED", "LEFT"})
        Enrollment.EnrollmentStatus status
) {}
