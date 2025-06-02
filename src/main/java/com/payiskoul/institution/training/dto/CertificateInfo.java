package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// CertificateInfo.java
@Schema(description = "Information sur un certificat")
public record CertificateInfo(
        @Schema(description = "ID du certificat")
        String id,

        @Schema(description = "Nom du certificat")
        String name,

        @Schema(description = "Offre associée")
        String offerTitle,

        @Schema(description = "Date d'obtention")
        LocalDateTime issuedAt
) {}
