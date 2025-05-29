package com.payiskoul.institution.exception;

import java.util.Map;

public class ServiceTimeoutException extends BusinessException {
    public ServiceTimeoutException(String message) {
        super(ErrorCode.SERVICE_TIMEOUT, message);
    }

    public ServiceTimeoutException(String message, Map<String, Object> details) {
        super(ErrorCode.SERVICE_TIMEOUT, message, details);
    }
}