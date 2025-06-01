package com.payiskoul.institution.tuition.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_references")
public class PaymentReference implements Serializable {
    @Id
    private String id;

    @Indexed(unique = true)
    private String reference;

    private String matricule;
    private String enrollmentId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paymentDate;
    private LocalDateTime processedAt;
}