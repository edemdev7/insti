package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// ApprovalResponse.java
@Schema(description = "Réponse d'approbation")
public record ApprovalResponse(
        @Schema(description = "ID de l'offre")
        String offerId,

        @Schema(description = "Approuvé")
        Boolean isApproved,

        @Schema(description = "Date d'approbation")
        LocalDateTime approvalDate,

        @Schema(description = "Message")
        String message
) {}
