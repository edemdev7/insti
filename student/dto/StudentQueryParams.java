package com.payiskoul.institution.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ============ QUERY PARAMS ============
@Schema(description = "Paramètres de recherche pour les étudiants")
public record StudentQueryParams(
        @Schema(description = "Numéro de page", example = "0")
        Integer page,

        @Schema(description = "Taille de la page", example = "10")
        Integer size
) {}
