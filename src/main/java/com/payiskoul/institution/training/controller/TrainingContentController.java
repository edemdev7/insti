package com.payiskoul.institution.training.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.training.dto.*;
import com.payiskoul.institution.training.model.TrainingQuiz;
import com.payiskoul.institution.training.service.QuizService;
import com.payiskoul.institution.training.service.TrainingContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/training")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contenu de formation", description = "API pour la gestion du contenu des formations (sections, leçons, progression)")
public class TrainingContentController {

    private final TrainingContentService trainingContentService;
    private final QuizService quizService;

    // ============ GESTION DES SECTIONS ============

    @PostMapping("/offers/{offerId}/sections")
    @Operation(
            summary = "Créer une section",
            description = "Crée une nouvelle section pour une offre de formation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Section créée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingSectionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Offre introuvable",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TrainingSectionResponse> createSection(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,
            @Valid @RequestBody CreateSectionRequest request) {

        log.info("Création d'une section pour l'offre {}: {}", offerId, request.title());
        TrainingSectionResponse response = trainingContentService.createSection(offerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/offers/{offerId}/sections")
    @Operation(
            summary = "Lister les sections",
            description = "Récupère toutes les sections d'une offre de formation"
    )
    public ResponseEntity<List<TrainingSectionResponse>> getSectionsByOffer(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId) {

        log.info("Récupération des sections pour l'offre {}", offerId);
        List<TrainingSectionResponse> sections = trainingContentService.getSectionsByOffer(offerId);
        return ResponseEntity.ok(sections);
    }

    @PutMapping("/offers/{offerId}/sections/{sectionId}")
    @Operation(
            summary = "Modifier une section",
            description = "Met à jour une section existante"
    )
    public ResponseEntity<TrainingSectionResponse> updateSection(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,
            @Parameter(description = "ID de la section", required = true)
            @PathVariable String sectionId,
            @Valid @RequestBody UpdateSectionRequest request) {

        log.info("Mise à jour de la section {} pour l'offre {}", sectionId, offerId);
        TrainingSectionResponse response = trainingContentService.updateSection(offerId, sectionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/offers/{offerId}/sections/{sectionId}")
    @Operation(
            summary = "Supprimer une section",
            description = "Supprime une section et toutes ses leçons"
    )
    public ResponseEntity<Void> deleteSection(
            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,
            @Parameter(description = "ID de la section", required = true)
            @PathVariable String sectionId) {

        log.info("Suppression de la section {} pour l'offre {}", sectionId, offerId);
        trainingContentService.deleteSection(offerId, sectionId);
        return ResponseEntity.noContent().build();
    }

    // ============ GESTION DES LEÇONS ============

    @PostMapping("/sections/{sectionId}/lectures")
    @Operation(
            summary = "Créer une leçon",
            description = "Crée une nouvelle leçon dans une section"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Leçon créée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingLectureResponse.class)))
    })
    public ResponseEntity<TrainingLectureResponse> createLecture(
            @Parameter(description = "ID de la section", required = true)
            @PathVariable String sectionId,
            @Valid @RequestBody CreateLectureRequest request) {

        log.info("Création d'une leçon pour la section {}: {}", sectionId, request.title());
        TrainingLectureResponse response = trainingContentService.createLecture(sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sections/{sectionId}/lectures")
    @Operation(
            summary = "Lister les leçons",
            description = "Récupère toutes les leçons d'une section"
    )
    public ResponseEntity<List<TrainingLectureResponse>> getLecturesBySection(
            @Parameter(description = "ID de la section", required = true)
            @PathVariable String sectionId) {

        log.info("Récupération des leçons pour la section {}", sectionId);
        List<TrainingLectureResponse> lectures = trainingContentService.getLecturesBySection(sectionId);
        return ResponseEntity.ok(lectures);
    }

    @PutMapping("/lectures/{lectureId}")
    @Operation(
            summary = "Modifier une leçon",
            description = "Met à jour une leçon existante"
    )
    public ResponseEntity<TrainingLectureResponse> updateLecture(
            @Parameter(description = "ID de la leçon", required = true)
            @PathVariable String lectureId,
            @Valid @RequestBody UpdateLectureRequest request) {

        log.info("Mise à jour de la leçon {}", lectureId);
        TrainingLectureResponse response = trainingContentService.updateLecture(lectureId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lectures/{lectureId}")
    @Operation(
            summary = "Supprimer une leçon",
            description = "Supprime une leçon"
    )
    public ResponseEntity<Void> deleteLecture(
            @Parameter(description = "ID de la leçon", required = true)
            @PathVariable String lectureId) {

        log.info("Suppression de la leçon {}", lectureId);
        trainingContentService.deleteLecture(lectureId);
        return ResponseEntity.noContent().build();
    }

    // ============ GESTION DE LA PROGRESSION ============

    @PutMapping("/enrollments/{enrollmentId}/lectures/{lectureId}/progress")
    @Operation(
            summary = "Mettre à jour la progression",
            description = "Met à jour la progression d'un étudiant pour une leçon"
    )
    public ResponseEntity<LectureProgressResponse> updateProgress(
            @Parameter(description = "ID de l'inscription", required = true)
            @PathVariable String enrollmentId,
            @Parameter(description = "ID de la leçon", required = true)
            @PathVariable String lectureId,
            @Valid @RequestBody UpdateProgressRequest request) {

        log.info("Mise à jour de la progression pour l'inscription {} et la leçon {}",
                enrollmentId, lectureId);
        LectureProgressResponse response = trainingContentService.updateProgress(
                enrollmentId, lectureId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrollments/{enrollmentId}/progress")
    @Operation(
            summary = "Récupérer la progression",
            description = "Récupère la progression d'un étudiant pour une offre"
    )
    public ResponseEntity<List<LectureProgressResponse>> getProgressByEnrollment(
            @Parameter(description = "ID de l'inscription", required = true)
            @PathVariable String enrollmentId) {

        log.info("Récupération de la progression pour l'inscription {}", enrollmentId);
        List<LectureProgressResponse> progress = trainingContentService.getProgressByEnrollment(enrollmentId);
        return ResponseEntity.ok(progress);
    }
    @GetMapping("/offers/{offerId}/content")
    @Operation(
            summary = "Récupérer le contenu complet d'une offre",
            description = "Équivalent de CourseDetailView de Django - récupère sections, leçons et progression"
    )
    public ResponseEntity<TrainingOfferContentResponse> getOfferContent(
            @PathVariable String offerId,
            @RequestParam(required = false) String studentId) {

        log.info("Récupération du contenu complet pour l'offre {} et l'étudiant {}", offerId, studentId);
        TrainingOfferContentResponse response = trainingContentService.getOfferContent(offerId, studentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offers/{offerId}/publish")
    @Operation(
            summary = "Publier une offre de formation",
            description = "Équivalent de CoursePublishView de Django"
    )
    public ResponseEntity<PublishResponse> publishOffer(
            @PathVariable String offerId,
            @Valid @RequestBody PublishOfferRequest request) {

        log.info("Publication de l'offre {}", offerId);
        PublishResponse response = trainingContentService.publishOffer(offerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offers/{offerId}/approve")
    @Operation(
            summary = "Approuver une offre",
            description = "Équivalent de CourseApprovalView de Django - admin uniquement"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalResponse> approveOffer(
            @PathVariable String offerId,
            @Valid @RequestBody ApprovalRequest request) {

        log.info("Approbation de l'offre {} : {}", offerId, request.isApproved());
        ApprovalResponse response = trainingContentService.approveOffer(offerId, request);
        return ResponseEntity.ok(response);
    }

    // ============ GESTION DES DOCUMENTS ============

    @PostMapping("/offers/{offerId}/documents")
    @Operation(summary = "Ajouter un document à une offre")
    public ResponseEntity<DocumentResponse> addDocument(
            @PathVariable String offerId,
            @Valid @RequestBody CreateDocumentRequest request) {

        DocumentResponse response = trainingContentService.addDocument(offerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/offers/{offerId}/documents")
    @Operation(summary = "Lister les documents d'une offre")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable String offerId) {
        List<DocumentResponse> documents = trainingContentService.getDocuments(offerId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{documentId}/download")
    @Operation(summary = "Télécharger un document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String documentId) {
        return trainingContentService.downloadDocument(documentId);
    }

    // ============ STATISTIQUES ET RAPPORTS ============

    @GetMapping("/offers/{offerId}/statistics")
    @Operation(
            summary = "Statistiques d'une offre",
            description = "Équivalent de calculate_course_stats de Django"
    )
    public ResponseEntity<OfferStatisticsResponse> getOfferStatistics(@PathVariable String offerId) {
        OfferStatisticsResponse stats = trainingContentService.calculateOfferStats(offerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/institutions/{institutionId}/dashboard")
    @Operation(
            summary = "Tableau de bord du formateur",
            description = "Équivalent de TrainerDashboardView de Django"
    )
    public ResponseEntity<InstitutionDashboardResponse> getInstitutionDashboard(
            @PathVariable String institutionId) {

        InstitutionDashboardResponse dashboard = trainingContentService.getInstitutionDashboard(institutionId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/students/{studentId}/dashboard")
    @Operation(
            summary = "Tableau de bord de l'étudiant",
            description = "Équivalent de StudentDashboardView de Django"
    )
    public ResponseEntity<StudentDashboardResponse> getStudentDashboard(@PathVariable String studentId) {
        StudentDashboardResponse dashboard = trainingContentService.getStudentDashboard(studentId);
        return ResponseEntity.ok(dashboard);
    }

    // ============ RAPPORTS CSV ============

    @GetMapping("/institutions/{institutionId}/reports/students")
    @Operation(
            summary = "Rapport CSV des étudiants",
            description = "Équivalent de generate_student_report de Django"
    )
    public ResponseEntity<Resource> generateStudentReport(@PathVariable String institutionId) {
        return trainingContentService.generateStudentReport(institutionId);
    }

    @GetMapping("/offers/{offerId}/reports/progress")
    @Operation(
            summary = "Rapport de progression pour une offre",
            description = "Équivalent de generate_course_report de Django"
    )
    public ResponseEntity<Resource> generateProgressReport(@PathVariable String offerId) {
        return trainingContentService.generateProgressReport(offerId);
    }
    // ============ GESTION DES QUIZ ============

    @PostMapping("/sections/{sectionId}/quizzes")
    @Operation(summary = "Créer un quiz pour une section")
    public ResponseEntity<QuizResponse> createSectionQuiz(
            @PathVariable String sectionId,
            @Valid @RequestBody QuizCreateRequest request) {

        QuizResponse response = quizService.createQuiz(sectionId, TrainingQuiz.ParentType.SECTION, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/lectures/{lectureId}/quizzes")
    @Operation(summary = "Créer un quiz pour une lecture")
    public ResponseEntity<QuizResponse> createLectureQuiz(
            @PathVariable String lectureId,
            @Valid @RequestBody QuizCreateRequest request) {

        QuizResponse response = quizService.createQuiz(lectureId, TrainingQuiz.ParentType.LECTURE, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/quizzes/attempt")
    @Operation(summary = "Soumettre une tentative de quiz")
    public ResponseEntity<QuizAttemptResponse> submitQuizAttempt(
            @Valid @RequestBody QuizAttemptRequest request) {

        QuizAttemptResponse response = quizService.submitQuizAttempt(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quizzes/{quizId}/attempts")
    @Operation(summary = "Récupérer les tentatives d'un quiz")
    public ResponseEntity<List<QuizAttemptResponse>> getQuizAttempts(
            @PathVariable String quizId,
            @RequestParam String enrollmentId) {

        List<QuizAttemptResponse> attempts = quizService.getStudentQuizAttempts(enrollmentId, quizId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/quizzes/{quizId}/statistics")
    @Operation(summary = "Statistiques d'un quiz")
    public ResponseEntity<QuizStatisticsResponse> getQuizStatistics(@PathVariable String quizId) {
        QuizStatisticsResponse stats = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(stats);
    }
}