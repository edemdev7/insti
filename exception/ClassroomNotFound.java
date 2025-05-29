package com.payiskoul.institution.exception;

import java.util.Map;

public class ClassroomNotFound extends BusinessException{
    public ClassroomNotFound(String message) {
        super(ErrorCode.CLASSROOM_NOT_FOUND, message);
    }

    public ClassroomNotFound(String message, Map<String, Object> details) {
        super(ErrorCode.CLASSROOM_NOT_FOUND, message, details);
    }
}
