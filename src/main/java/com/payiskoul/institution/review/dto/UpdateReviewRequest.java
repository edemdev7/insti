package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Demande de mise à jour d'évaluation")
public record UpdateReviewRequest(
        @Min(value = 1, message = "La note doit être entre 1 et 5")
        @Max(value = 5, message = "La note doit être entre 1 et 5")
        @Schema(description = "Note sur 5", example = "4")
        Integer rating,

        @Size(min = 10, max = 1000, message = "Le commentaire doit faire entre 10 et 1000 caractères")
        @Schema(description = "Commentaire sur l'offre")
        String comment,

        @Schema(description = "Recommande l'offre", example = "true")
        Boolean recommended
) {}

