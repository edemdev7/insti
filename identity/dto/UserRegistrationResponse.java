package com.payiskoul.institution.identity.dto;

public record UserRegistrationResponse(
        String email,
        String userId,
        String message
) {}