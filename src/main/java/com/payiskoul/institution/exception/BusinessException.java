package com.payiskoul.institution.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all business exceptions in the application
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : new HashMap<>();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    public BusinessException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    //TODO: handle HttpMessageNotReadableException (400 bad Request)
}

