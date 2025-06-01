package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information sur une classe")
public record ClassroomInfo(
        @Schema(description = "ID de la classe", example = "664f82a9e9d034c2fca9b0e8")
        String id,

        @Schema(description = "Nom de la classe", example = "Licence 1 A")
        String name,

        @Schema(description = "Nombre d'étudiants actuels", example = "10")
        int currentCount,

        @Schema(description = "Capacité maximale", example = "30")
        int capacity
) {}
