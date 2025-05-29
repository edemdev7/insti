package com.payiskoul.institution.banking.dto;

public record CustomerInfo(
        String id,
        String fullName,
        String country,
        String phone,
        String currency,
        String customerType
) {}