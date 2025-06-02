package com.payiskoul.institution.review.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.review.dto.*;
import com.payiskoul.institution.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des avis sur les offres de formation
 * Équivalent du système Review de Django
 */
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Avis et évaluations", description = "API pour la gestion des avis sur les offres de formation")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/offers/{offerId}")
    @Operation(
            summary = "Créer un avis sur une offre",
            description = """
                    Permet à un étudiant inscrit de laisser un avis sur une offre de formation.
                    
                    **Prérequis :**
                    - L'étudiant doit être inscrit à l'offre
                    - L'étudiant ne doit pas avoir déjà laissé d'avis
                    - La note doit être entre 1 et 5
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Avis créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou avis déjà existant",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Étudiant non inscrit à cette offre",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,

            @Parameter(description = "ID de l'étudiant", required = true)
            @RequestParam String studentId,

            @Valid @RequestBody CreateReviewRequest request) {

        log.info("Création d'un avis pour l'offre {} par l'étudiant {}", offerId, studentId);
        ReviewResponse response = reviewService.createReview(offerId, studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/offers/{offerId}")
    @Operation(
            summary = "Lister les avis d'une offre",
            description = "Récupère tous les avis pour une offre de formation avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des avis récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReviewListResponse.class))
            )
    })
    public ResponseEntity<ReviewListResponse> getReviewsByOffer(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,

            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Récupération des avis pour l'offre {} - page: {}, size: {}", offerId, page, size);
        ReviewListResponse response = reviewService.getReviewsByOffer(offerId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/offers/{offerId}/statistics")
    @Operation(
            summary = "Statistiques des avis d'une offre",
            description = "Récupère les statistiques des avis : note moyenne, distribution des notes, etc."
    )
    public ResponseEntity<ReviewStatisticsResponse> getReviewStatistics(@PathVariable String offerId) {
        log.info("Récupération des statistiques d'avis pour l'offre {}", offerId);
        ReviewStatisticsResponse stats = reviewService.getReviewStatistics(offerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/students/{studentId}")
    @Operation(
            summary = "Avis laissés par un étudiant",
            description = "Récupère tous les avis laissés par un étudiant"
    )
    public ResponseEntity<List<ReviewResponse>> getReviewsByStudent(@PathVariable String studentId) {
        log.info("Récupération des avis de l'étudiant {}", studentId);
        List<ReviewResponse> reviews = reviewService.getReviewsByStudent(studentId);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
    @Operation(
            summary = "Modifier un avis",
            description = "Permet à un étudiant de modifier son avis existant"
    )
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        log.info("Mise à jour de l'avis {}", reviewId);
        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "Supprimer un avis",
            description = "Permet à un étudiant de supprimer son avis"
    )
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        log.info("Suppression de l'avis {}", reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @Operation(
            summary = "Récupérer l'avis d'une inscription",
            description = "Récupère l'avis laissé pour une inscription spécifique"
    )
    public ResponseEntity<ReviewResponse> getReviewByEnrollment(@PathVariable String enrollmentId) {
        log.info("Récupération de l'avis pour l'inscription {}", enrollmentId);
        ReviewResponse review = reviewService.getReviewByEnrollment(enrollmentId);
        return ResponseEntity.ok(review);
    }
}