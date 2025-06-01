package com.payiskoul.institution.classroom.dto;

import com.payiskoul.institution.program.dto.DurationUnit;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Information sur un programme")
public record ProgramInfo(
        @Schema(description = "ID du programme", example = "6632138e5a875b301f9d402a")
        String id,

        @Schema(description = "Nom du programme", example = "Licence 1")
        String name,

        @Schema(description = "Année académique", example = "2025-2026")
        String academicYear,
        @Schema(description = "Durée du programme", example = "1")
        int duration,

        @Schema(description = "Unité de temps pour la durée", example = "Year")
        DurationUnit durationUnit,


        @Schema(description = "Nombre total de participants", example = "50")
        int totalStudents

) {}