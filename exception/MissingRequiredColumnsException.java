package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les colonnes manquantes dans un fichier importé
 */
public class MissingRequiredColumnsException extends BusinessException {
    public MissingRequiredColumnsException(String message, Map<String, Object> details) {
        super(ErrorCode.MISSING_REQUIRED_COLUMNS, message, details);
    }
}
