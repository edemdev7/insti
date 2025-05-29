package com.payiskoul.institution.organization.dto;

import com.payiskoul.institution.organization.model.Address;
import com.payiskoul.institution.organization.model.Contact;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.model.InstitutionType;
import org.apache.catalina.User;

public record InstitutionResponse(
        String id,
//        String externalId,
        String name,
        String acronym,
        InstitutionType type,
        Address address,
        Contact contact,
        String website,
        String description,
        InstitutionStatus status,
        UserData user ,
        String createdAt,
        String updatedAt
) {}
