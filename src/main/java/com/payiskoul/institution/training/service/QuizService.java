package com.payiskoul.institution.training.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.training.dto.*;
import com.payiskoul.institution.training.model.*;
import com.payiskoul.institution.training.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final TrainingQuizRepository trainingQuizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final TrainingSectionRepository trainingSectionRepository;
    private final TrainingLectureRepository trainingLectureRepository;

    /**
     * Crée un quiz pour une section ou une lecture
     */
    @Transactional
    public QuizResponse createQuiz(String parentId, TrainingQuiz.ParentType parentType,
                                   QuizCreateRequest request) {
        log.info("Création d'un quiz pour {} {}: {}", parentType, parentId, request.title());

        // Vérifier que le parent existe
        validateParentExists(parentId, parentType);

        // Créer le quiz
        TrainingQuiz quiz = TrainingQuiz.builder()
                .parentId(parentId)
                .parentType(parentType)
                .title(request.title())
                .description(request.description())
                .questions(request.questions())
                .passingScore(request.passingScore() != null ? request.passingScore() : 70)
                .timeLimit(request.timeLimit())
                .maxAttempts(request.maxAttempts() != null ? request.maxAttempts() : 3)
                .shuffleQuestions(request.shuffleQuestions() != null ? request.shuffleQuestions() : false)
                .showAnswersImmediately(request.showAnswersImmediately() != null ?
                        request.showAnswersImmediately() : true)
                .createdAt(LocalDateTime.now())
                .build();

        TrainingQuiz savedQuiz = trainingQuizRepository.save(quiz);
        log.info("Quiz créé avec succès: {}", savedQuiz.getId());

        return mapToQuizResponse(savedQuiz);
    }

    /**
     * Récupère tous les quiz d'une section ou lecture
     */
    public List<QuizResponse> getQuizzesByParent(String parentId, TrainingQuiz.ParentType parentType) {
        log.info("Récupération des quiz pour {} {}", parentType, parentId);

        List<TrainingQuiz> quizzes = trainingQuizRepository.findByParentIdAndParentType(parentId, parentType);
        return quizzes.stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Soumet une tentative de quiz
     */
    @Transactional
    public QuizAttemptResponse submitQuizAttempt(QuizAttemptRequest request) {
        log.info("Soumission d'une tentative de quiz {} par l'inscription {}",
                request.quizId(), request.enrollmentId());

        // Récupérer le quiz
        TrainingQuiz quiz = trainingQuizRepository.findById(request.quizId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Quiz introuvable", Map.of("quizId", request.quizId())));

        // Vérifier le nombre de tentatives
        long attemptCount = quizAttemptRepository.countByEnrollmentIdAndQuizId(
                request.enrollmentId(), request.quizId());

        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Nombre maximum de tentatives atteint pour ce quiz",
                    Map.of("maxAttempts", quiz.getMaxAttempts(), "currentAttempts", attemptCount));
        }

        // Calculer le score
        QuizResult result = calculateQuizScore(quiz, request.answers());

        // Créer la tentative
        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(request.quizId())
                .enrollmentId(request.enrollmentId())
                .attemptNumber((int) attemptCount + 1)
                .answers(request.answers())
                .score(result.score())
                .percentage(result.percentage())
                .passed(result.percentage() >= quiz.getPassingScore())
                .timeSpent(request.timeSpent())
                .startedAt(LocalDateTime.now().minusSeconds(request.timeSpent() != null ? request.timeSpent() : 0))
                .submittedAt(LocalDateTime.now())
                .status(QuizAttempt.AttemptStatus.COMPLETED)
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        log.info("Tentative de quiz sauvegardée: score={}, passed={}",
                result.percentage(), savedAttempt.getPassed());

        return mapToAttemptResponse(savedAttempt, result.correctAnswers());
    }

    /**
     * Récupère les tentatives d'un étudiant pour un quiz
     */
    public List<QuizAttemptResponse> getStudentQuizAttempts(String enrollmentId, String quizId) {
        log.info("Récupération des tentatives pour l'inscription {} et le quiz {}", enrollmentId, quizId);

        List<QuizAttempt> attempts = quizAttemptRepository
                .findByEnrollmentIdAndQuizIdOrderByAttemptNumberDesc(enrollmentId, quizId);

        return attempts.stream()
                .map(attempt -> mapToAttemptResponse(attempt, null)) // Pas besoin des réponses correctes ici
                .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques d'un quiz
     */
    public QuizStatisticsResponse getQuizStatistics(String quizId) {
        log.info("Calcul des statistiques pour le quiz {}", quizId);

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);

        if (attempts.isEmpty()) {
            return new QuizStatisticsResponse(quizId, 0, 0.0, 0.0, 0, 0, Map.of());
        }

        int totalAttempts = attempts.size();
        double averageScore = attempts.stream()
                .mapToDouble(QuizAttempt::getPercentage)
                .average()
                .orElse(0.0);

        double passRate = attempts.stream()
                .mapToDouble(attempt -> attempt.getPassed() ? 1.0 : 0.0)
                .average()
                .orElse(0.0) * 100;

        int uniqueStudents = (int) attempts.stream()
                .map(QuizAttempt::getEnrollmentId)
                .distinct()
                .count();

        int passedStudents = (int) attempts.stream()
                .filter(QuizAttempt::getPassed)
                .map(QuizAttempt::getEnrollmentId)
                .distinct()
                .count();

        // Distribution des scores par tranche de 10%
        Map<String, Integer> scoreDistribution = attempts.stream()
                .collect(Collectors.groupingBy(
                        attempt -> {
                            int range = (int) (attempt.getPercentage() / 10) * 10;
                            return range + "-" + (range + 9) + "%";
                        },
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        return new QuizStatisticsResponse(
                quizId, totalAttempts, averageScore, passRate,
                uniqueStudents, passedStudents, scoreDistribution
        );
    }

    // ============ MÉTHODES PRIVÉES ============

    private void validateParentExists(String parentId, TrainingQuiz.ParentType parentType) {
        switch (parentType) {
            case SECTION -> {
                if (!trainingSectionRepository.existsById(parentId)) {
                    throw new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                            "Section introuvable", Map.of("sectionId", parentId));
                }
            }
            case LECTURE -> {
                if (!trainingLectureRepository.existsById(parentId)) {
                    throw new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                            "Lecture introuvable", Map.of("lectureId", parentId));
                }
            }
        }
    }

    private QuizResult calculateQuizScore(TrainingQuiz quiz, Map<Integer, List<String>> studentAnswers) {
        List<QuizQuestion> questions = quiz.getQuestions();
        double correctAnswers = 0;
        Map<Integer, List<String>> correctAnswersMap = new HashMap<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            List<String> studentAnswer = studentAnswers.getOrDefault(i, List.of());

            // Stocker les bonnes réponses
            List<String> correctAnswer = getCorrectAnswersForQuestion(question);
            correctAnswersMap.put(i, correctAnswer);

            // Vérifier si la réponse est correcte
            if (isAnswerCorrect(question, studentAnswer)) {
                correctAnswers++;
            }
        }

        double percentage = (correctAnswers / questions.size()) * 100;

        return new QuizResult(correctAnswers, percentage, correctAnswersMap);
    }

    private List<String> getCorrectAnswersForQuestion(QuizQuestion question) {
        switch (question.getType()) {
            case MULTIPLE_CHOICE, MULTIPLE_SELECT -> {
                if (question.getCorrectAnswers() != null && question.getOptions() != null) {
                    return question.getCorrectAnswers().stream()
                            .map(index -> question.getOptions().get(index))
                            .collect(Collectors.toList());
                }
            }
            case TRUE_FALSE, SHORT_ANSWER, ESSAY -> {
                return List.of(question.getCorrectAnswer());
            }
        }
        return List.of();
    }

    private boolean isAnswerCorrect(QuizQuestion question, List<String> studentAnswer) {
        List<String> correctAnswers = getCorrectAnswersForQuestion(question);

        switch (question.getType()) {
            case MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER -> {
                return studentAnswer.size() == 1 &&
                        correctAnswers.contains(studentAnswer.get(0));
            }
            case MULTIPLE_SELECT -> {
                return new HashSet<>(studentAnswer).equals(new HashSet<>(correctAnswers));
            }
            case ESSAY -> {
                // Pour les questions ouvertes, on pourrait implémenter une logique plus complexe
                return studentAnswer.size() == 1 &&
                        studentAnswer.get(0).toLowerCase().contains(
                                correctAnswers.get(0).toLowerCase());
            }
        }
        return false;
    }

    // ============ MÉTHODES DE MAPPING ============

    private QuizResponse mapToQuizResponse(TrainingQuiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getParentType(),
                quiz.getParentId(),
                quiz.getQuestions(),
                quiz.getPassingScore(),
                quiz.getTimeLimit(),
                quiz.getMaxAttempts(),
                quiz.getShuffleQuestions(),
                quiz.getShowAnswersImmediately(),
                quiz.getCreatedAt()
        );
    }

    private QuizAttemptResponse mapToAttemptResponse(QuizAttempt attempt,
                                                     Map<Integer, List<String>> correctAnswers) {
        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getEnrollmentId(),
                attempt.getScore(),
                attempt.getPercentage(),
                attempt.getPassed(),
                attempt.getAnswers(),
                correctAnswers,
                attempt.getTimeSpent(),
                attempt.getAttemptNumber(),
                attempt.getSubmittedAt()
        );
    }

    // ============ RECORDS INTERNES ============

    private record QuizResult(double score, double percentage, Map<Integer, List<String>> correctAnswers) {}
}