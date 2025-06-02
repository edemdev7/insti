package com.payiskoul.institution.training.dto;

import com.payiskoul.institution.training.model.LectureResource;
import com.payiskoul.institution.training.model.TrainingLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

// ============ SECTION DTOs ============

@Schema(description = "Demande de création d'une section")
public record CreateSectionRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Schema(description = "Titre de la section", example = "Introduction au DevOps")
        String title,

        @Schema(description = "Description de la section")
        String description,

        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre doit être supérieur à 0")
        @Schema(description = "Ordre d'affichage", example = "1")
        Integer order,

        @Schema(description = "Durée estimée en minutes", example = "120")
        Integer durationMinutes,

        @Schema(description = "Aperçu gratuit", example = "false")
        Boolean isFreePreview
) {}



