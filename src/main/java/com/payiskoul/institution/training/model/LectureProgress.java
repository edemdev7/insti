package com.payiskoul.institution.training.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime; /**
 * Représente la progression d'un étudiant dans une leçon
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lecture_progress")
public class LectureProgress implements Serializable {
    @Id
    private String id;

    /**
     * ID de l'inscription de l'étudiant
     */
    private String enrollmentId;

    /**
     * ID de l'étudiant
     */
    private String studentId;

    /**
     * ID de la leçon
     */
    private String lectureId;

    /**
     * Indique si la leçon est terminée
     */
    @Builder.Default
    private Boolean isCompleted = false;

    /**
     * Pourcentage de progression (0-100)
     */
    @Builder.Default
    private Integer progressPercent = 0;

    /**
     * Position actuelle dans la leçon (en secondes pour une vidéo)
     */
    @Builder.Default
    private Integer currentPosition = 0;

    /**
     * Temps total passé sur cette leçon (en secondes)
     */
    @Builder.Default
    private Integer timeSpent = 0;

    /**
     * Date de première consultation
     */
    private LocalDateTime firstAccessedAt;

    /**
     * Date de dernière consultation
     */
    private LocalDateTime lastAccessedAt;

    /**
     * Date de completion
     */
    private LocalDateTime completedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Marque la leçon comme terminée
     */
    public void markAsCompleted() {
        this.isCompleted = true;
        this.progressPercent = 100;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
