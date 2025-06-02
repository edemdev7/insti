package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// OfferBasicInfo.java
@Schema(description = "Informations de base d'une offre")
public record OfferBasicInfo(
        @Schema(description = "ID de l'offre")
        String id,

        @Schema(description = "Titre de l'offre")
        String title,

        @Schema(description = "Type d'offre")
        String offerType,

        @Schema(description = "Prix")
        BigDecimal price,

        @Schema(description = "Devise")
        String currency,

        @Schema(description = "Publié")
        Boolean isPublished,

        @Schema(description = "Approuvé")
        Boolean isApproved
) {}
