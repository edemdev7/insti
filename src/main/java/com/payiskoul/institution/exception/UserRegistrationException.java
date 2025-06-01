package com.payiskoul.institution.exception;

import java.util.Map;

/**
 * Exception levée lors d'échecs d'enregistrement d'utilisateurs pour une institution
 */
public class UserRegistrationException extends BusinessException {

    public UserRegistrationException(String message) {
        super(ErrorCode.USER_REGISTRATION_FAILED, message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(ErrorCode.USER_REGISTRATION_FAILED, message);
        this.initCause(cause);
    }

    public UserRegistrationException(String message, Map<String, Object> details) {
        super(ErrorCode.USER_REGISTRATION_FAILED, message, details);
    }

    public UserRegistrationException(String message, Map<String, Object> details, Throwable cause) {
        super(ErrorCode.USER_REGISTRATION_FAILED, message, details);
        this.initCause(cause);
    }
}