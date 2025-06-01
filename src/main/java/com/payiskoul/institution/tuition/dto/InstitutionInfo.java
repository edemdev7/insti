package com.payiskoul.institution.tuition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Informations sur une institution")
public record InstitutionInfo(
        @Schema(description = "ID unique de l'institution", example = "660a7cc3fa021b4d94b0e63b")
        String id,

        @Schema(description = "Nom de l'institution", example = "Université de Cocody")
        String name
) {
}