package com.payiskoul.institution.training.repository;

import com.payiskoul.institution.training.model.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    /**
     * Trouve toutes les tentatives d'un étudiant pour un quiz
     */
    List<QuizAttempt> findByEnrollmentIdAndQuizIdOrderByAttemptNumberDesc(String enrollmentId, String quizId);

    /**
     * Compte le nombre de tentatives d'un étudiant pour un quiz
     */
    long countByEnrollmentIdAndQuizId(String enrollmentId, String quizId);

    /**
     * Trouve la meilleure tentative d'un étudiant pour un quiz
     */
    @Query("{ 'enrollmentId': ?0, 'quizId': ?1 }")
    Optional<QuizAttempt> findBestAttemptByEnrollmentIdAndQuizId(String enrollmentId, String quizId);

    /**
     * Trouve toutes les tentatives d'un quiz
     */
    List<QuizAttempt> findByQuizId(String quizId);

    /**
     * Trouve toutes les tentatives d'un étudiant
     */
    List<QuizAttempt> findByEnrollmentId(String enrollmentId);
}