package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ============ SUPPORTING RECORDS ============
@Schema(description = "Information sur une institution")
public record InstitutionInfo(
        @Schema(description = "ID de l'institution", example = "664f82a9e9d034c2fca9b0e2")
        String id,

        @Schema(description = "Nom de l'institution", example = "ESATIC")
        String name
) {}
