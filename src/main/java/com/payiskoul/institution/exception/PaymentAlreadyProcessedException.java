package com.payiskoul.institution.exception;

import java.util.Map;

public class PaymentAlreadyProcessedException extends BusinessException {
    public PaymentAlreadyProcessedException(String message) {
        super(ErrorCode.PAYMENT_ALREADY_PROCESSED, message);
    }

    public PaymentAlreadyProcessedException(String message, Map<String, Object> details) {
        super(ErrorCode.PAYMENT_ALREADY_PROCESSED, message, details);
    }
}