package com.payiskoul.institution.training.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Représente une section/chapitre d'une offre de formation
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "training_sections")
public class TrainingSection implements Serializable {
    @Id
    private String id;

    /**
     * ID de l'offre de formation à laquelle appartient cette section
     */
    private String trainingOfferId;

    /**
     * Titre de la section
     */
    private String title;

    /**
     * Description de la section
     */
    private String description;

    /**
     * Ordre d'affichage dans l'offre
     */
    private Integer order;

    /**
     * Durée estimée de la section (en minutes)
     */
    private Integer durationMinutes;

    /**
     * Indique si cette section est un aperçu gratuit
     */
    @Builder.Default
    private Boolean isFreePreview = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

