package com.payiskoul.institution.training.dto;

import com.payiskoul.institution.training.model.LectureResource;
import com.payiskoul.institution.training.model.TrainingLecture;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Réponse contenant les informations d'une leçon")
public record TrainingLectureResponse(
        @Schema(description = "ID de la leçon")
        String id,

        @Schema(description = "Titre de la leçon")
        String title,

        @Schema(description = "Contenu textuel")
        String content,

        @Schema(description = "Type de leçon")
        TrainingLecture.LectureType type,

        @Schema(description = "URL de la vidéo")
        String videoUrl,

        @Schema(description = "URL du fichier joint")
        String attachmentUrl,

        @Schema(description = "Nom du fichier joint")
        String attachmentName,

        @Schema(description = "Durée en minutes")
        Integer durationMinutes,

        @Schema(description = "Ordre d'affichage")
        Integer order,

        @Schema(description = "Aperçu gratuit")
        Boolean isFreePreview,

        @Schema(description = "Ressources supplémentaires")
        List<LectureResource> resources,

        @Schema(description = "Date de création")
        LocalDateTime createdAt
) {}
