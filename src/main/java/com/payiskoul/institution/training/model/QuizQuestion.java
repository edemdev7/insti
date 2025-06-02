package com.payiskoul.institution.training.model;

import lombok.*;

import java.io.Serializable;
import java.util.List; /**
 * Représente une question de quiz
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion implements Serializable {
    /**
     * Texte de la question
     */
    private String questionText;

    /**
     * Type de question
     */
    private QuestionType type;

    /**
     * Options de réponse (pour QCM)
     */
    private List<String> options;

    /**
     * Indices des bonnes réponses (pour les options)
     */
    private List<Integer> correctAnswers;

    /**
     * Réponse correcte (pour les questions ouvertes)
     */
    private String correctAnswer;

    /**
     * Points attribués pour cette question
     */
    @Builder.Default
    private Integer points = 1;

    /**
     * Explication de la réponse
     */
    private String explanation;

    public enum QuestionType {
        MULTIPLE_CHOICE,     // QCM à choix unique
        MULTIPLE_SELECT,     // QCM à choix multiples
        TRUE_FALSE,          // Vrai/Faux
        SHORT_ANSWER,        // Réponse courte
        ESSAY               // Réponse longue
    }
}
