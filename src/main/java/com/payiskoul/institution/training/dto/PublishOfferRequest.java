package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// PublishOfferRequest.java
@Schema(description = "Demande de publication d'une offre")
public record PublishOfferRequest(
        @Schema(description = "Publier l'offre", example = "true")
        Boolean isPublished
) {}
