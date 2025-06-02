package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Réponse contenant les informations d'une section")
public record TrainingSectionResponse(
        @Schema(description = "ID de la section")
        String id,

        @Schema(description = "Titre de la section")
        String title,

        @Schema(description = "Description de la section")
        String description,

        @Schema(description = "Ordre d'affichage")
        Integer order,

        @Schema(description = "Durée estimée en minutes")
        Integer durationMinutes,

        @Schema(description = "Aperçu gratuit")
        Boolean isFreePreview,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
