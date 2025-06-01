package com.payiskoul.institution.organization.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "institutions")
public class Institution implements Serializable {
    @Id
    private String id;

    private String userId;

    private String externalId;

    private String name;
    private String acronym;

    private InstitutionType type;
    private Address address;
    private Contact contact;
    private String website;
    private String description;
    private String zoneId;
    private String accountId;

    @Builder.Default
    private InstitutionStatus status = InstitutionStatus.ACTIVE;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt ;
}
