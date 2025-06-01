package com.payiskoul.institution.banking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AccountCreationRequest(
        String id,
        String fullName,
        String country,
        String phone,
        String currency,
        String customerType,
        String pin
) {}