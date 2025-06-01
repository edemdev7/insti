package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception pour les fichiers trop volumineux
 */
public class MaxFileSizeExceededException extends BusinessException {
    public MaxFileSizeExceededException(String message, Map<String, Object> details) {
        super(ErrorCode.MAX_FILE_SIZE_EXCEEDED, message, details);
    }
}
