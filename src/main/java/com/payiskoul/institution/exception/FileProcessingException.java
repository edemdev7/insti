package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les erreurs de traitement de fichier
 */
public class FileProcessingException extends BusinessException {
    public FileProcessingException(String message, Map<String, Object> details) {
        super(ErrorCode.FILE_PROCESSING_ERROR, message, details);
    }
}
