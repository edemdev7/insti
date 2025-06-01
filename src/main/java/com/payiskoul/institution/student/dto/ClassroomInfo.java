package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ============ CLASSROOM INFO ============
@Schema(description = "Informations sur une classe")
public record ClassroomInfo(
        @Schema(description = "ID de la classe", example = "1203ed41de")
        String id,

        @Schema(description = "Nom de la classe", example = "L1-A")
        String name
) {}
