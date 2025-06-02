package com.payiskoul.institution.training.dto;

import com.payiskoul.institution.training.model.LectureResource;
import com.payiskoul.institution.training.model.TrainingLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Demande de création d'une leçon")
public record CreateLectureRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Schema(description = "Titre de la leçon", example = "Installation de Docker")
        String title,

        @Schema(description = "Contenu textuel de la leçon")
        String content,

        @NotNull(message = "Le type de leçon est obligatoire")
        @Schema(description = "Type de leçon")
        TrainingLecture.LectureType type,

        @Schema(description = "URL de la vidéo")
        String videoUrl,

        @Schema(description = "URL du fichier joint")
        String attachmentUrl,

        @Schema(description = "Nom du fichier joint")
        String attachmentName,

        @Schema(description = "Durée en minutes", example = "45")
        Integer durationMinutes,

        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre doit être supérieur à 0")
        @Schema(description = "Ordre d'affichage", example = "1")
        Integer order,

        @Schema(description = "Aperçu gratuit", example = "false")
        Boolean isFreePreview,

        @Schema(description = "Ressources supplémentaires")
        List<LectureResource> resources
) {}
