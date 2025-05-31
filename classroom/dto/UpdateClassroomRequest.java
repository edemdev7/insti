package com.payiskoul.institution.classroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Demande de mise à jour d'une classe")
public record UpdateClassroomRequest(
        @Schema(description = "Nom de la classe", example = "Classe A")
        @NotBlank(message = "Le nom de la classe est obligatoire")
        String name,

        @Schema(description = "Capacité d'accueil de la classe", example = "35")
        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité doit être supérieure à 0")
        Integer capacity
) {}