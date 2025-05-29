package com.payiskoul.institution.exception;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Structure standardisée de réponse en cas d'erreur")
public record ErrorResponse(
        @Schema(description = "Horodatage de l'erreur", example = "2025-04-05T12:00:00Z")
        Instant timestamp,

        @Schema(description = "Code d'erreur technique", example = "INSTITUTION_NOT_FOUND")
        String errorCode,

        @Schema(description = "Message d'erreur explicatif", example = "L'institution avec l'ID spécifié n'a pas été trouvée")
        String message,

        @Schema(description = "Chemin de la requête ayant généré l'erreur", example = "/v1/institutions/123")
        String path,

        @Schema(description = "Détails supplémentaires spécifiques à l'erreur")
        Map<String, Object> details
) {
    public static ErrorResponse fromBusinessException(BusinessException ex, WebRequest request) {
        return new ErrorResponse(
                Instant.now(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getDescription(false),
                ex.getDetails()
        );
    }

    public ErrorResponse {
        if (details == null) {
            details = Map.of();
        }
    }
}
