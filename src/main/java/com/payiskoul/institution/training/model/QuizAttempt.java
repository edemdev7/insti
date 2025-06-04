package com.payiskoul.institution.training.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Représente une tentative de quiz par un étudiant
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quiz_attempts")
public class QuizAttempt implements Serializable {
    @Id
    private String id;

    /**
     * ID du quiz
     */
    private String quizId;

    /**
     * ID de l'inscription de l'étudiant
     */
    private String enrollmentId;

    /**
     * ID de l'étudiant
     */
    private String studentId;

    /**
     * Numéro de la tentative (1, 2, 3, etc.)
     */
    private Integer attemptNumber;

    /**
     * Réponses données par l'étudiant
     * Map<questionIndex, selectedAnswers>
     */
    private Map<Integer, List<String>> answers;

    /**
     * Score obtenu (nombre de bonnes réponses)
     */
    private Double score;

    /**
     * Pourcentage obtenu
     */
    private Double percentage;

    /**
     * Quiz réussi ou non
     */
    private Boolean passed;

    /**
     * Temps passé sur le quiz (en secondes)
     */
    private Integer timeSpent;

    /**
     * Date de début
     */
    private LocalDateTime startedAt;

    /**
     * Date de soumission
     */
    @CreatedDate
    private LocalDateTime submittedAt;

    /**
     * Statut de la tentative
     */
    @Builder.Default
    private AttemptStatus status = AttemptStatus.COMPLETED;

    public enum AttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        ABANDONED,
        TIME_EXPIRED
    }
}