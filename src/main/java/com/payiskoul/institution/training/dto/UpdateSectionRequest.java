package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Demande de mise à jour d'une section")
public record UpdateSectionRequest(
        @Schema(description = "Titre de la section")
        String title,

        @Schema(description = "Description de la section")
        String description,

        @Schema(description = "Ordre d'affichage")
        Integer order,

        @Schema(description = "Durée estimée en minutes")
        Integer durationMinutes,

        @Schema(description = "Aperçu gratuit")
        Boolean isFreePreview
) {}
