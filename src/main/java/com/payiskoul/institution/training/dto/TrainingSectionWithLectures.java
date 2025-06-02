package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// TrainingSectionWithLectures.java
@Schema(description = "Section avec ses leçons")
public record TrainingSectionWithLectures(
        @Schema(description = "Informations de la section")
        TrainingSectionResponse section,

        @Schema(description = "Liste des leçons")
        List<TrainingLectureResponse> lectures
) {}
