package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les erreurs de validation des données importées
 */
public class ImportDataValidationException extends BusinessException {
    public ImportDataValidationException(String message, Map<String, Object> details) {
        super(ErrorCode.IMPORT_DATA_VALIDATION_ERROR, message, details);
    }
}