package com.payiskoul.institution.identity.dto;

import lombok.Builder;

public record UserRegistrationRequest(
        String email,
        String password,
        String role,
        String institutionId
) {
    @Builder
    public UserRegistrationRequest {}
}