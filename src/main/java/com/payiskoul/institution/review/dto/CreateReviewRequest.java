package com.payiskoul.institution.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(description = "Demande de création d'évaluation")
public record CreateReviewRequest(
        @NotNull(message = "La note est obligatoire")
        @Min(value = 1, message = "La note doit être entre 1 et 5")
        @Max(value = 5, message = "La note doit être entre 1 et 5")
        @Schema(description = "Note sur 5", example = "4")
        Integer rating,

        @NotBlank(message = "Le commentaire est obligatoire")
        @Size(min = 10, max = 1000, message = "Le commentaire doit faire entre 10 et 1000 caractères")
        @Schema(description = "Commentaire sur l'offre")
        String comment,

        @Schema(description = "Recommande l'offre", example = "true")
        Boolean recommended
) {}

