package com.payiskoul.institution.training.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List; /**
 * Représente un quiz/évaluation dans une offre de formation
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "training_quizzes")
public class TrainingQuiz implements Serializable {
    @Id
    private String id;

    /**
     * ID de la section ou de la leçon
     */
    private String parentId;

    /**
     * Type de parent (SECTION ou LECTURE)
     */
    private ParentType parentType;

    /**
     * Titre du quiz
     */
    private String title;

    /**
     * Description du quiz
     */
    private String description;

    /**
     * Questions du quiz
     */
    private List<QuizQuestion> questions;

    /**
     * Score minimum pour réussir (pourcentage)
     */
    @Builder.Default
    private Integer passingScore = 70;

    /**
     * Durée limite du quiz (en minutes)
     */
    private Integer timeLimit;

    /**
     * Nombre maximum de tentatives autorisées
     */
    @Builder.Default
    private Integer maxAttempts = 3;

    /**
     * Indique si les questions sont mélangées
     */
    @Builder.Default
    private Boolean shuffleQuestions = false;

    /**
     * Indique si les réponses sont affichées immédiatement
     */
    @Builder.Default
    private Boolean showAnswersImmediately = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ParentType {
        SECTION, LECTURE
    }
}
