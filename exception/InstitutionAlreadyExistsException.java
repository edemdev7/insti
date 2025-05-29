package com.payiskoul.institution.exception;

import java.util.Map;

public class InstitutionAlreadyExistsException extends BusinessException {

    public InstitutionAlreadyExistsException(String message) {
        super(ErrorCode.INSTITUTION_ALREADY_EXISTS, message);
    }

    public InstitutionAlreadyExistsException(String message, Map<String, Object> details) {
        super(ErrorCode.INSTITUTION_ALREADY_EXISTS, message, details);
    }
}
