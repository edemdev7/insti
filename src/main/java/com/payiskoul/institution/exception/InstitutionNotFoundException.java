package com.payiskoul.institution.exception;

import java.util.Map;

public class InstitutionNotFoundException extends BusinessException {

    public InstitutionNotFoundException(String message) {
        super(ErrorCode.INSTITUTION_NOT_FOUND, message);
    }

    public InstitutionNotFoundException(String message, Map<String, Object> details) {
        super(ErrorCode.INSTITUTION_NOT_FOUND, message, details);
    }
}