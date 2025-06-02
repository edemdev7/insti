package com.payiskoul.institution.review.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review implements Serializable {
    @Id
    private String id;

    /**
     * ID de l'inscription (pour vérifier que l'étudiant est bien inscrit).
     */
    private String enrollmentId;

    /**
     * ID de l'offre évaluée
     */
    private String trainingOfferId;

    /**
     * ID de l'étudiant qui évalue
     */
    private String studentId;

    /**
     * Note sur 5
     */
    private Integer rating;

    /**
     * Commentaire
     */
    private String comment;

    /**
     * Évaluation recommande l'offre
     */
    private Boolean recommended;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}