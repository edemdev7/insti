package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Contenu complet d'une offre de formation")
public record TrainingOfferContentResponse(
        @Schema(description = "Informations de base de l'offre")
        OfferBasicInfo offer,

        @Schema(description = "Sections du cours avec leçons")
        List<TrainingSectionWithLectures> sections,

        @Schema(description = "Progression de l'étudiant (si connecté)")
        StudentProgressSummary progress,

        @Schema(description = "Statistiques de l'offre")
        OfferStatisticsResponse statistics
) {}




//

