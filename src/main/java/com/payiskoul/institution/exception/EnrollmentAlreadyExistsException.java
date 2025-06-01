package com.payiskoul.institution.exception;

import java.util.Map;

public class EnrollmentAlreadyExistsException extends BusinessException {
    public EnrollmentAlreadyExistsException(String message) {
        super(ErrorCode.ENROLLMENT_ALREADY_EXISTS, message);
    }

    public EnrollmentAlreadyExistsException(String message, Map<String, Object> details) {
        super(ErrorCode.ENROLLMENT_ALREADY_EXISTS, message, details);
    }
}