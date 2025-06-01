package com.payiskoul.institution.exception;

import java.util.Map;

public class StudentAlreadyExistsException extends BusinessException {
    public StudentAlreadyExistsException(String message) {
        super(ErrorCode.STUDENT_ALREADY_EXISTS, message);
    }

    public StudentAlreadyExistsException(String message, Map<String, Object> details) {
        super(ErrorCode.STUDENT_ALREADY_EXISTS, message, details);
    }
}