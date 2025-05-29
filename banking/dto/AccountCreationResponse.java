package com.payiskoul.institution.banking.dto;

import java.time.LocalDate;

public record AccountCreationResponse(
        String accountId,
        String accountNumber,
        LocalDate openDate,
        String status,
        double balance,
        String currency,
        String name,
        String goal,
        CustomerInfo customer
) {}
