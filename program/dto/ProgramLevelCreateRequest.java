package com.payiskoul.institution.program.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProgramLevelCreateRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Schema(description = "Nom du niveau", example = "Licence 2")
        String name,

        @NotBlank(message = "L'année académique est obligatoire")
        @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "L'année académique doit être au format YYYY-YYYY")
        @Schema(description = "Année académique au format YYYY-YYYY", example = "2025-2026")
        String academicYear,

        @NotNull(message = "Les frais de scolarité sont obligatoires")
        @Valid
        @Schema(description = "Frais de scolarité")
        TuitionDto tuition,

        @Min(value = 1, message = "La durée doit être au minimum égale à 1")
        @Schema(description = "Durée du programme", example = "1")
        int duration,

        @NotNull(message = "L'unité de temps est obligatoire")
        @Schema(description = "Unit of the duration",
                allowableValues = {"year", "month", "day", "hour"},
                example = "year")
        DurationUnit durationUnit,

        @Schema(description = "Type de certification obtenue", example = "Licence")
        String certification
) {}
