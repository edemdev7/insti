package com.payiskoul.institution.exception;

import java.util.Map;

public class StudentNotFoundException extends BusinessException {
    public StudentNotFoundException(String message) {
        super(ErrorCode.STUDENT_NOT_FOUND, message);
    }

    public StudentNotFoundException(String message, Map<String, Object> details) {
        super(ErrorCode.STUDENT_NOT_FOUND, message, details);
    }
}
