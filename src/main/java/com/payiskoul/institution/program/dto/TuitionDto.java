package com.payiskoul.institution.program.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TuitionDto(
        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        @Schema(description = "Montant des frais de scolarité", example = "250000")
        BigDecimal amount,

        @NotNull(message = "La devise est obligatoire")
        @Schema(description = "Devise des frais de scolarité", example = "XOF")
        String currency
) {}
