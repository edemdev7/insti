package com.payiskoul.institution.review.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.review.dto.*;
import com.payiskoul.institution.review.model.Review;
import com.payiskoul.institution.review.repository.ReviewRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service complet pour la gestion des avis - équivalent Django Review
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TrainingOfferRepository trainingOfferRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    /**
     * Crée un avis pour une offre - équivalent de ReviewCreateView Django
     */
    @Transactional
    public ReviewResponse createReview(String offerId, String studentId, CreateReviewRequest request) {
        log.info("Création d'un avis pour l'offre {} par l'étudiant {}", offerId, studentId);

        // Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifier que l'étudiant existe
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                        "Étudiant introuvable", Map.of("studentId", studentId)));

        // Vérifier que l'étudiant est inscrit à cette offre
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        Enrollment enrollment = enrollments.stream()
                .filter(e -> e.getProgramLevelId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT,
                        "Vous devez être inscrit à cette offre pour laisser un avis",
                        Map.of("studentId", studentId, "offerId", offerId)));

        // Vérifier que l'étudiant n'a pas déjà laissé d'avis
        if (reviewRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_DATA,
                    "Vous avez déjà laissé un avis pour cette offre",
                    Map.of("enrollmentId", enrollment.getId()));
        }

        // Créer l'avis
        Review review = Review.builder()
                .enrollmentId(enrollment.getId())
                .trainingOfferId(offerId)
                .studentId(studentId)
                .rating(request.rating())
                .comment(request.comment())
                .recommended(request.recommended())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Avis créé avec succès: {}", savedReview.getId());

        // Mettre à jour les statistiques de l'offre
        updateOfferRatingStats(offerId);

        return mapToReviewResponse(savedReview, student, offer);
    }

    /**
     * Récupère les avis d'une offre avec pagination - équivalent ReviewListView Django
     */
    public ReviewListResponse getReviewsByOffer(String offerId, int page, int size) {
        log.info("Récupération des avis pour l'offre {} - page: {}, size: {}", offerId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewsPage = reviewRepository.findByTrainingOfferIdOrderByCreatedAtDesc(offerId, pageable);

        List<ReviewResponse> reviews = reviewsPage.getContent().stream()
                .map(this::mapToReviewResponseWithDetails)
                .collect(Collectors.toList());

        return new ReviewListResponse(
                reviewsPage.getNumber(),
                reviewsPage.getSize(),
                reviewsPage.getTotalElements(),
                reviewsPage.getTotalPages(),
                reviews
        );
    }

    /**
     * Calcule les statistiques des avis - équivalent calculate_course_stats Django
     */
    public ReviewStatisticsResponse getReviewStatistics(String offerId) {
        log.info("Calcul des statistiques d'avis pour l'offre {}", offerId);

        List<Review> reviews = reviewRepository.findByTrainingOfferId(offerId);

        if (reviews.isEmpty()) {
            return new ReviewStatisticsResponse(0.0, 0, Map.of(), 0.0);
        }

        // Calculer la note moyenne
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Distribution des notes
        Map<Integer, Integer> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        // Pourcentage de recommandations
        long recommendedCount = reviews.stream()
                .filter(review -> Boolean.TRUE.equals(review.getRecommended()))
                .count();
        double recommendationPercentage = (double) recommendedCount / reviews.size() * 100;

        return new ReviewStatisticsResponse(
                averageRating,
                reviews.size(),
                ratingDistribution,
                recommendationPercentage
        );
    }

    /**
     * Récupère les avis laissés par un étudiant
     */
    public List<ReviewResponse> getReviewsByStudent(String studentId) {
        log.info("Récupération des avis de l'étudiant {}", studentId);

        // Récupérer toutes les inscriptions de l'étudiant
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        List<ReviewResponse> reviews = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Optional<Review> reviewOpt = reviewRepository.findByEnrollmentId(enrollment.getId());
            if (reviewOpt.isPresent()) {
                Review review = reviewOpt.get();
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                                "Étudiant introuvable", Map.of("studentId", studentId)));

                TrainingOffer offer = trainingOfferRepository.findById(enrollment.getProgramLevelId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                                "Offre introuvable", Map.of("offerId", enrollment.getProgramLevelId())));

                reviews.add(mapToReviewResponse(review, student, offer));
            }
        }

        return reviews;
    }

    /**
     * Met à jour un avis existant
     */
    @Transactional
    public ReviewResponse updateReview(String reviewId, UpdateReviewRequest request) {
        log.info("Mise à jour de l'avis {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Avis introuvable", Map.of("reviewId", reviewId)));

        // Mettre à jour les champs
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        if (request.comment() != null) {
            review.setComment(request.comment());
        }
        if (request.recommended() != null) {
            review.setRecommended(request.recommended());
        }

        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        log.info("Avis mis à jour avec succès: {}", updatedReview.getId());

        // Mettre à jour les statistiques de l'offre
        updateOfferRatingStats(review.getTrainingOfferId());

        // Récupérer les informations pour la réponse
        Student student = studentRepository.findById(review.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                        "Étudiant introuvable", Map.of("studentId", review.getStudentId())));

        TrainingOffer offer = trainingOfferRepository.findById(review.getTrainingOfferId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", review.getTrainingOfferId())));

        return mapToReviewResponse(updatedReview, student, offer);
    }

    /**
     * Supprime un avis
     */
    @Transactional
    public void deleteReview(String reviewId) {
        log.info("Suppression de l'avis {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Avis introuvable", Map.of("reviewId", reviewId)));

        String offerId = review.getTrainingOfferId();
        reviewRepository.delete(review);

        // Mettre à jour les statistiques de l'offre
        updateOfferRatingStats(offerId);

        log.info("Avis supprimé avec succès: {}", reviewId);
    }

    /**
     * Récupère l'avis d'une inscription spécifique
     */
    public ReviewResponse getReviewByEnrollment(String enrollmentId) {
        log.info("Récupération de l'avis pour l'inscription {}", enrollmentId);

        Review review = reviewRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Aucun avis trouvé pour cette inscription", Map.of("enrollmentId", enrollmentId)));

        return mapToReviewResponseWithDetails(review);
    }

// ============ MÉTHODES PRIVÉES ============

    /**
     * Met à jour les statistiques de notation d'une offre
     */
    private void updateOfferRatingStats(String offerId) {
        List<Review> reviews = reviewRepository.findByTrainingOfferId(offerId);

        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            // Mettre à jour l'offre avec les nouvelles statistiques
            TrainingOffer offer = trainingOfferRepository.findById(offerId)
                    .orElse(null);
            if (offer != null) {
                offer.updateRating(averageRating);
                trainingOfferRepository.save(offer);
            }
        }
    }

    /**
     * Mappe un avis vers le DTO de réponse avec détails complets
     */
    private ReviewResponse mapToReviewResponseWithDetails(Review review) {
        // Récupérer l'étudiant
        Student student = studentRepository.findById(review.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                        "Étudiant introuvable", Map.of("studentId", review.getStudentId())));

        // Récupérer l'offre
        TrainingOffer offer = trainingOfferRepository.findById(review.getTrainingOfferId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", review.getTrainingOfferId())));

        return mapToReviewResponse(review, student, offer);
    }

    /**
     * Mappe un avis vers le DTO de réponse
     */
    private ReviewResponse mapToReviewResponse(Review review, Student student, TrainingOffer offer) {
        StudentInfo studentInfo = new StudentInfo(
                student.getId(),
                student.getFullName(),
                student.getMatricule()
        );

        return new ReviewResponse(
                review.getId(),
                studentInfo,
                review.getRating(),
                review.getComment(),
                review.getRecommended(),
                review.getCreatedAt()
        );
    }
}