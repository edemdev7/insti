package com.payiskoul.institution.exception;

import java.util.Map;

public class ProgramLevelAlreadyExistsException extends BusinessException {

    public ProgramLevelAlreadyExistsException(String message) {
        super(ErrorCode.PROGRAM_LEVEL_ALREADY_EXISTS, message);
    }

    public ProgramLevelAlreadyExistsException(String message, Map<String, Object> details) {
        super(ErrorCode.PROGRAM_LEVEL_ALREADY_EXISTS, message, details);
    }
}
