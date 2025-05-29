package com.payiskoul.institution.tuition.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tuition_status")
public class TuitionStatus implements Serializable {
    @Id
    private String id;

    private String enrollmentId;
    private String studentId;
    private String matricule;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String currency;

    private PaymentStatus paymentStatus;

    private LocalDateTime lastUpdatedAt;

}