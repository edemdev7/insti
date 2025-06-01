package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// ============ OFFER INFO ============
@Schema(description = "Informations sur une offre")
public record OfferInfo(
        @NotBlank(message = "L'ID de l'offre est obligatoire")
        @Schema(description = "ID de l'offre", example = "664f82a9e9d034f2fca9b0e2")
        String id,

        @NotBlank(message = "Le titre de l'offre est obligatoire")
        @Schema(description = "Titre de l'offre", example = "Master 1")
        String title
) {}
