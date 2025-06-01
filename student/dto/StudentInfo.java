package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// ============ STUDENT INFO ============
@Schema(description = "Informations sur un étudiant")
public record StudentInfo(
        @Schema(description = "ID de l'étudiant", example = "634f82a9e9d034c2fca9b0e2")
        String id,

        @NotBlank(message = "Le nom complet est obligatoire")
        @Schema(description = "Nom complet de l'étudiant", example = "Assi Mason")
        String fullname,
        Object o) {}
