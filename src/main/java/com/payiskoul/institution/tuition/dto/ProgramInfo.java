package com.payiskoul.institution.tuition.dto;

import com.payiskoul.institution.tuition.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
@Schema(description = "Informations sur un programme d'études")
public record ProgramInfo(
        @Schema(description = "ID unique du programme", example = "661be293e90f256128f7e203")
        String id,

        @Schema(description = "Code du programme", example = "L1")
        String code,

        @Schema(description = "Nom du programme", example = "Licence 1")
        String name,

        @Schema(description = "Année académique", example = "2024-2025")
        String academicYear,

        @Schema(description = "Information sur l'institution")
        InstitutionInfo institution
) {
}
