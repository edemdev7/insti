package com.payiskoul.institution.classroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Réponse contenant les informations d'une classe")
public record ClassroomResponse(
        @Schema(description = "ID unique de la classe", example = "663214f45a875b301f9d4033")
        String id,

        @Schema(description = "Nom de la classe", example = "Classe A")
        String name,

        @Schema(description = "Capacité d'accueil de la classe", example = "25")
        int capacity,
        @Schema(description = "Nombre total actuel d'élèves dans la classe", example = "25")
        int currentCount,

        @Schema(description = "Date de création", example = "2025-01-23")
        LocalDate createdAt
) {}
