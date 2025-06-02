package com.payiskoul.institution.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Demande de création d'un document")
public record CreateDocumentRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Schema(description = "Titre du document", example = "Guide d'installation")
        String title,

        @Schema(description = "Description du document")
        String description,

        @NotBlank(message = "L'URL est obligatoire")
        @Schema(description = "URL du fichier")
        String fileUrl,

        @Schema(description = "Taille du fichier")
        Long fileSize,

        @Schema(description = "Type MIME")
        String mimeType,

        @Schema(description = "Document public", example = "true")
        Boolean isPublic
) {}

