package com.payiskoul.institution.training.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List; /**
 * Représente une leçon/lecture individuelle dans une section
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "training_lectures")
public class TrainingLecture implements Serializable {
    @Id
    private String id;

    /**
     * ID de la section à laquelle appartient cette leçon
     */
    private String sectionId;

    /**
     * Titre de la leçon
     */
    private String title;

    /**
     * Contenu textuel de la leçon
     */
    private String content;

    /**
     * Type de contenu
     */
    private LectureType type;

    /**
     * URL de la vidéo (si applicable)
     */
    private String videoUrl;

    /**
     * URL du fichier de pièce jointe (si applicable)
     */
    private String attachmentUrl;

    /**
     * Nom du fichier de pièce jointe
     */
    private String attachmentName;

    /**
     * Durée de la leçon (en minutes)
     */
    private Integer durationMinutes;

    /**
     * Ordre d'affichage dans la section
     */
    private Integer order;

    /**
     * Indique si cette leçon est un aperçu gratuit
     */
    @Builder.Default
    private Boolean isFreePreview = false;

    /**
     * Ressources supplémentaires
     */
    private List<LectureResource> resources;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Types de leçons supportés
     */
    public enum LectureType {
        VIDEO,           // Leçon vidéo
        TEXT,            // Leçon textuelle
        DOCUMENT,        // Document/PDF
        QUIZ,            // Quiz/évaluation
        ASSIGNMENT,      // Exercice/devoir
        LIVE_SESSION,    // Session en direct
        MIXED            // Contenu mixte
    }
}
