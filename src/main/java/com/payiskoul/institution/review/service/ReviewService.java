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
import java.util.List;
import java.util.Map;
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
            return new ReviewStatisticsResponse(0.0, 0, Map.of