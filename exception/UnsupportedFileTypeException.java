package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les types de fichiers non supportés
 */
public class UnsupportedFileTypeException extends BusinessException {
    public UnsupportedFileTypeException(String message, Map<String, Object> details) {
        super(ErrorCode.UNSUPPORTED_FILE_TYPE, message, details);
    }
}

