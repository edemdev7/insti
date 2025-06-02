package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// PublishResponse.java
@Schema(description = "Réponse de publication")
public record PublishResponse(
        @Schema(description = "ID de l'offre")
        String offerId,

        @Schema(description = "Publié")
        Boolean isPublished,

        @Schema(description = "Message")
        String message
) {}
