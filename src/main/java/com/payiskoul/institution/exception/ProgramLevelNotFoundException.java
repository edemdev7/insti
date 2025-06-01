package com.payiskoul.institution.exception;

import java.util.Map;

public class ProgramLevelNotFoundException extends BusinessException {
    public ProgramLevelNotFoundException(String message) {
        super(ErrorCode.PROGRAM_LEVEL_NOT_FOUND, message);
    }

    public ProgramLevelNotFoundException(String message, Map<String, Object> details) {
        super(ErrorCode.PROGRAM_LEVEL_NOT_FOUND, message, details);
    }
}
