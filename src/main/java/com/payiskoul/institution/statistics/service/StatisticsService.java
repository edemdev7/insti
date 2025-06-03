// src/main/java/com/payiskoul/institution/statistics/service/StatisticsService.java
package com.payiskoul.institution.statistics.service;

import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.review.model.Review;
import com.payiskoul.institution.review.repository.ReviewRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.training.model.LectureProgress;
import com.payiskoul.institution.training.repository.LectureProgressRepository;
import com.payiskoul.institution.tuition.model.TuitionStatus;
import com.payiskoul.institution.tuition.repository.TuitionStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service équivalent à calculate_course_stats de Django
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final TuitionStatusRepository tuitionStatusRepository;

    /**
     * Calcule les statistiques complètes d'une offre de formation
     * Équivalent de la fonction calculate_course_stats de Django
     */
    public Map<String, Object> calculateOfferStatistics(TrainingOffer offer) {
        log.info("Calcul des statistiques pour l'offre: {}", offer.getId());

        Map<String, Object> stats = new HashMap<>();

        // Statistiques de base
        long totalStudents = enrollmentRepository.countByProgramLevelId(offer.getId());
        stats.put("total_students", totalStudents);

        // Statistiques des avis
        List<Review> reviews = reviewRepository.findByTrainingOfferId(offer.getId());
        stats.put("total_reviews", reviews.size());

        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            stats.put("average_rating", Math.round(averageRating * 10.0) / 10.0);

            // Distribution des notes
            Map<Integer, Long> ratingDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                long count = reviews.stream()
                        .filter(r -> r.getRating().equals(rating))
                        .count();
                ratingDistribution.put(rating, count);
            }
            stats.put("rating_distribution", ratingDistribution);

            // Pourcentage de recommandations
            long recommendedCount = reviews.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getRecommended()))
                    .count();
            double recommendationRate = (double) recommendedCount / reviews.size() * 100;
            stats.put("recommendation_rate", Math.round(recommendationRate * 10.0) / 10.0);
        } else {
            stats.put("average_rating", 0.0);
            stats.put("rating_distribution", Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L));
            stats.put("recommendation_rate", 0.0);
        }

        // Taux de complétion (progression)
        if (totalStudents > 0) {
            double completionRate = calculateCompletionRate(offer.getId());
            stats.put("completion_rate", Math.round(completionRate * 10.0) / 10.0);
        } else {
            stats.put("completion_rate", 0.0);
        }

        // Statistiques financières
        Map<String, Object> financialStats = calculateFinancialStats(offer.getId());
        stats.putAll(financialStats);

        // Statistiques temporelles
        Map<String, Object> timeStats = calculateTimeStats(offer.getId());
        stats.putAll(timeStats);

        log.info("Statistiques calculées pour l'offre {}: {} étudiants, {} avis",
                offer.getId(), totalStudents, reviews.size());

        return stats;
    }

    /**
     * Calcule le taux de complétion moyen
     */
    private double calculateCompletionRate(String offerId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // TODO: Méthode à corriger

        if (enrollments.isEmpty()) {
            return 0.0;
        }

        double totalProgress = 0.0;
        int validEnrollments = 0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getProgramLevelId().equals(offerId)) {
                List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollment.getId());

                if (!progressList.isEmpty()) {
                    double enrollmentProgress = progressList.stream()
                            .mapToInt(LectureProgress::getProgressPercent)
                            .average()
                            .orElse(0.0);

                    totalProgress += enrollmentProgress;
                    validEnrollments++;
                }
            }
        }

        return validEnrollments > 0 ? totalProgress / validEnrollments : 0.0;
    }

    /**
     * Calcule les statistiques financières
     */
    private Map<String, Object> calculateFinancialStats(String offerId) {
        Map<String, Object> financialStats = new HashMap<>();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // TODO: Méthode à corriger
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        long paidStudents = 0;
        long partiallyPaidStudents = 0;
        long unpaidStudents = 0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getProgramLevelId().equals(offerId)) {
                List<TuitionStatus> tuitionStatuses = tuitionStatusRepository.findByEnrollmentId(enrollment.getId());

                if (!tuitionStatuses.isEmpty()) {
                    TuitionStatus status = tuitionStatuses.get(0);
                    totalRevenue = totalRevenue.add(status.getPaidAmount());
                    totalPending = totalPending.add(status.getRemainingAmount());

                    switch (status.getPaymentStatus()) {
                        case PAID -> paidStudents++;
                        case PARTIALLY_PAID -> partiallyPaidStudents++;
                        case UNPAID -> unpaidStudents++;
                    }
                }
            }
        }

        financialStats.put("total_revenue", totalRevenue);
        financialStats.put("total_pending", totalPending);
        financialStats.put("paid_students", paidStudents);
        financialStats.put("partially_paid_students", partiallyPaidStudents);
        financialStats.put("unpaid_students", unpaidStudents);

        return financialStats;
    }

    /**
     * Calcule les statistiques temporelles
     */
    private Map<String, Object> calculateTimeStats(String offerId) {
        Map<String, Object> timeStats = new HashMap<>();

        // Inscriptions récentes (7 derniers jours)
        long recentEnrollments = 0; // TODO: Implémenter la logique avec des dates

        // Activité récente
        long activeStudentsThisWeek = 0; // TODO: Implémenter la logique

        timeStats.put("recent_enrollments", recentEnrollments);
        timeStats.put("active_students_this_week", activeStudentsThisWeek);

        return timeStats;
    }

    /**
     * Calcule les statistiques pour un étudiant spécifique
     */
    public Map<String, Object> calculateStudentStatistics(String studentId, String offerId) {
        Map<String, Object> stats = new HashMap<>();

        // Progression générale
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getProgramLevelId().equals(offerId)) {
                List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollment.getId());

                int totalLectures = progressList.size();
                long completedLectures = progressList.stream()
                        .filter(LectureProgress::getIsCompleted)
                        .count();

                double overallProgress = totalLectures > 0 ?
                        (double) completedLectures / totalLectures * 100 : 0.0;

                stats.put("total_lectures", totalLectures);
                stats.put("completed_lectures", completedLectures);
                stats.put("overall_progress", Math.round(overallProgress * 10.0) / 10.0);

                // Temps total passé
                int totalTimeSpent = progressList.stream()
                        .mapToInt(LectureProgress::getTimeSpent)
                        .sum();
                stats.put("total_time_spent", totalTimeSpent);

                break;
            }
        }

        return stats;
    }

    /**
     * Calcule les statistiques globales d'une institution
     */
    public Map<String, Object> calculateInstitutionStatistics(String institutionId) {
        Map<String, Object> stats = new HashMap<>();

        // TODO: Implémenter les statistiques globales de l'institution
        // - Nombre total d'offres
        // - Nombre total d'étudiants
        // - Revenus totaux
        // - Notes moyennes
        // etc.

        return stats;
    }
}