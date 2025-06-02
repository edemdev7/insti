package com.payiskoul.institution.review.repository;

import com.payiskoul.institution.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByTrainingOfferIdOrderByCreatedAtDesc(String trainingOfferId, Pageable pageable);
    Optional<Review> findByEnrollmentId(String enrollmentId);
    boolean existsByEnrollmentId(String enrollmentId);
    List<Review> findByTrainingOfferId(String trainingOfferId);
    long countByTrainingOfferId(String trainingOfferId);
}