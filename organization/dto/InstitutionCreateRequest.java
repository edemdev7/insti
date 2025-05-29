package com.payiskoul.institution.organization.dto;

import com.payiskoul.institution.organization.model.Address;
import com.payiskoul.institution.organization.model.Contact;
import com.payiskoul.institution.organization.model.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InstitutionCreateRequest(
        @NotBlank(message = "L'identifiant de l'utilisateur  est obligatoire")
        String userId,

        @NotBlank(message = "L'identifiant de l'utilisateur  est obligatoire")
        String accountId,

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        String acronym,

        @NotNull(message = "Le type d'institution est obligatoire")
        InstitutionType type,

        Address address,

        Contact contact,

        String website,

        String description,

        @NotBlank(message = "PIN is required")
        @Size(min = 5, max = 5, message = "PIN must be 5 digits")
        @Pattern(regexp = "\\d{5}", message = "PIN must contain only digits")
        String pin
) {}
