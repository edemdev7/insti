package com.payiskoul.institution.program.model;



import com.payiskoul.institution.program.dto.DurationUnit;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "offers")
@CompoundIndex(name = "idx_institution_name_year",
                def = "{'institutionId': 1, 'name': 1, 'academicYear': 1}",
                unique = true)
public class Offers implements Serializable {
    @Id
    private String id;

    private String code;
    private String name;
    private String academicYear;

    private String institutionId;

    private Tuition tuition;
    private int duration;
    private DurationUnit durationUnit;
    private String certification;

    private OfferType offerType;
    private LocalDateTime lastPaymentDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}