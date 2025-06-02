package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// DocumentResponse.java
@Schema(description = "Réponse document")
public record DocumentResponse(
        @Schema(description = "ID du document")
        String id,

        @Schema(description = "Titre du document")
        String title,

        @Schema(description = "URL de téléchargement")
        String downloadUrl,

        @Schema(description = "Taille du fichier")
        Long fileSize,

        @Schema(description = "Type MIME")
        String mimeType,

        @Schema(description = "Public ou privé")
        Boolean isPublic,

        @Schema(description = "Nombre de téléchargements")
        Integer downloadCount
) {}
