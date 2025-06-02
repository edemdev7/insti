package com.payiskoul.institution.program.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// ============ OFFER APPROVAL REQUEST ============
@Schema(description = "Demande d'approbation d'une offre")
public record OfferApprovalRequest(
        @NotNull(message = "Le statut d'approbation est obligatoire")
        @Schema(description = "Statut d'approbation", example = "true")
        Boolean isApproved,

        @Schema(description = "Raison du rejet (si applicable)")
        String rejectionReason
) {}
