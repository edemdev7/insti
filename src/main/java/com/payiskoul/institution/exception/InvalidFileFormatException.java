package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les erreurs de format de fichier
 */
public class InvalidFileFormatException extends BusinessException {
    public InvalidFileFormatException(String message, Map<String, Object> details) {
        super(ErrorCode.INVALID_FILE_FORMAT, message, details);
    }
}
