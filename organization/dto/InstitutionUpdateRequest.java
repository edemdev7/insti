package com.payiskoul.institution.organization.dto;

import com.payiskoul.institution.organization.model.Address;
import com.payiskoul.institution.organization.model.Contact;

public record InstitutionUpdateRequest(
        Address address,
        Contact contact,
        String website,
        String description
) {}