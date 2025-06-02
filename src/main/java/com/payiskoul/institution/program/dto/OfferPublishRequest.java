package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// ============ OFFER PUBLISH REQUEST ============
@Schema(description = "Demande de publication d'une offre")
public record OfferPublishRequest(
        @NotNull(message = "Le statut de publication est obligatoire")
        @Schema(description = "Statut de publication", example = "true")
        Boolean isPublished
) {}
