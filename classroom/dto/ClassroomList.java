package com.payiskoul.institution.classroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "Réponse contenant les informations de chaque classe du programme")
public record ClassroomList(
        @Schema(description = "Information sur le programme associé")
        ProgramInfo program,
        @Schema(description = "Liste des classed du programme")
        List<ClassroomResponse>classrooms) {
}
