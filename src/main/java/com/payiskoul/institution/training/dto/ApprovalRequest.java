package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ApprovalRequest.java
@Schema(description = "Demande d'approbation d'une offre")
public record ApprovalRequest(
        @Schema(description = "Approuver l'offre", example = "true")
        Boolean isApproved,

        @Schema(description = "Raison du rejet (si applicable)")
        String rejectionReason
) {}
