package com.payiskoul.institution.training.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.review.model.Review;
import com.payiskoul.institution.review.repository.ReviewRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.training.dto.*;
import com.payiskoul.institution.training.model.LectureProgress;
import com.payiskoul.institution.training.model.TrainingLecture;
import com.payiskoul.institution.training.model.TrainingQuiz;
import com.payiskoul.institution.training.model.TrainingSection;
import com.payiskoul.institution.training.repository.LectureProgressRepository;
import com.payiskoul.institution.training.repository.TrainingLectureRepository;
import com.payiskoul.institution.training.repository.TrainingQuizRepository;
import com.payiskoul.institution.training.repository.TrainingSectionRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la gestion du contenu des formations (sections, leçons, quiz)
 * Équivalent des services Course, CourseSection, Lecture de Django
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingContentService {

    private final TrainingOfferRepository trainingOfferRepository;
    private final TrainingSectionRepository trainingSectionRepository;
    private final TrainingLectureRepository trainingLectureRepository;
    private final TrainingQuizRepository trainingQuizRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;

    // ============ GESTION DES SECTIONS ============

    /**
     * Crée une nouvelle section pour une offre de formation
     */
    @Transactional
    @CacheEvict(value = "trainingSections", key = "#offerId")
    public TrainingSectionResponse createSection(String offerId, CreateSectionRequest request) {
        log.info("Création d'une section pour l'offre {}: {}", offerId, request.title());

        // Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Créer la section
        TrainingSection section = TrainingSection.builder()
                .trainingOfferId(offerId)
                .title(request.title())
                .description(request.description())
                .order(request.order())
                .durationMinutes(request.durationMinutes())
                .isFreePreview(request.isFreePreview() != null ? request.isFreePreview() : false)
                .createdAt(LocalDateTime.now())
                .build();

        TrainingSection savedSection = trainingSectionRepository.save(section);
        log.info("Section créée avec succès: {}", savedSection.getId());

        return mapToSectionResponse(savedSection);
    }

    /**
     * Récupère toutes les sections d'une offre
     */
    @Cacheable(value = "trainingSections", key = "#offerId")
    public List<TrainingSectionResponse> getSectionsByOffer(String offerId) {
        log.info("Récupération des sections pour l'offre {}", offerId);

        List<TrainingSection> sections = trainingSectionRepository.findByTrainingOfferIdOrderByOrder(offerId);
        return sections.stream()
                .map(this::mapToSectionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une section
     */
    @Transactional
    @CacheEvict(value = "trainingSections", key = "#offerId")
    public TrainingSectionResponse updateSection(String offerId, String sectionId,
                                                 UpdateSectionRequest request) {
        log.info("Mise à jour de la section {} pour l'offre {}", sectionId, offerId);

        TrainingSection section = trainingSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Section introuvable", Map.of("sectionId", sectionId)));

        // Vérifier que la section appartient à l'offre
        if (!section.getTrainingOfferId().equals(offerId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette section n'appartient pas à cette offre",
                    Map.of("sectionId", sectionId, "offerId", offerId));
        }

        // Mettre à jour les champs
        if (request.title() != null) section.setTitle(request.title());
        if (request.description() != null) section.setDescription(request.description());
        if (request.order() != null) section.setOrder(request.order());
        if (request.durationMinutes() != null) section.setDurationMinutes(request.durationMinutes());
        if (request.isFreePreview() != null) section.setIsFreePreview(request.isFreePreview());

        section.setUpdatedAt(LocalDateTime.now());

        TrainingSection updatedSection = trainingSectionRepository.save(section);
        return mapToSectionResponse(updatedSection);
    }

    /**
     * Supprime une section et toutes ses leçons
     */
    @Transactional
    @CacheEvict(value = "trainingSections", key = "#offerId")
    public void deleteSection(String offerId, String sectionId) {
        log.info("Suppression de la section {} pour l'offre {}", sectionId, offerId);

        TrainingSection section = trainingSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Section introuvable", Map.of("sectionId", sectionId)));

        // Vérifier que la section appartient à l'offre
        if (!section.getTrainingOfferId().equals(offerId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette section n'appartient pas à cette offre",
                    Map.of("sectionId", sectionId, "offerId", offerId));
        }

        // Supprimer toutes les leçons de cette section
        List<TrainingLecture> lectures = trainingLectureRepository.findBySectionIdOrderByOrder(sectionId);
        for (TrainingLecture lecture : lectures) {
            deleteLectureData(lecture.getId());
        }
        trainingLectureRepository.deleteAll(lectures);

        // Supprimer la section
        trainingSectionRepository.delete(section);
        log.info("Section supprimée avec succès: {}", sectionId);
    }

    // ============ GESTION DES LEÇONS ============

    /**
     * Crée une nouvelle leçon dans une section
     */
    @Transactional
    public TrainingLectureResponse createLecture(String sectionId, CreateLectureRequest request) {
        log.info("Création d'une leçon pour la section {}: {}", sectionId, request.title());

        // Vérifier que la section existe
        TrainingSection section = trainingSectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Section introuvable", Map.of("sectionId", sectionId)));

        // Créer la leçon
        TrainingLecture lecture = TrainingLecture.builder()
                .sectionId(sectionId)
                .title(request.title())
                .content(request.content())
                .type(request.type())
                .videoUrl(request.videoUrl())
                .attachmentUrl(request.attachmentUrl())
                .attachmentName(request.attachmentName())
                .durationMinutes(request.durationMinutes())
                .order(request.order())
                .isFreePreview(request.isFreePreview() != null ? request.isFreePreview() : false)
                .resources(request.resources())
                .createdAt(LocalDateTime.now())
                .build();

        TrainingLecture savedLecture = trainingLectureRepository.save(lecture);
        log.info("Leçon créée avec succès: {}", savedLecture.getId());

        return mapToLectureResponse(savedLecture);
    }

    /**
     * Récupère toutes les leçons d'une section
     */
    public List<TrainingLectureResponse> getLecturesBySection(String sectionId) {
        log.info("Récupération des leçons pour la section {}", sectionId);

        List<TrainingLecture> lectures = trainingLectureRepository.findBySectionIdOrderByOrder(sectionId);
        return lectures.stream()
                .map(this::mapToLectureResponse)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une leçon
     */
    @Transactional
    public TrainingLectureResponse updateLecture(String lectureId, UpdateLectureRequest request) {
        log.info("Mise à jour de la leçon {}", lectureId);

        TrainingLecture lecture = trainingLectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Leçon introuvable", Map.of("lectureId", lectureId)));

        // Mettre à jour les champs
        if (request.title() != null) lecture.setTitle(request.title());
        if (request.content() != null) lecture.setContent(request.content());
        if (request.type() != null) lecture.setType(request.type());
        if (request.videoUrl() != null) lecture.setVideoUrl(request.videoUrl());
        if (request.attachmentUrl() != null) lecture.setAttachmentUrl(request.attachmentUrl());
        if (request.attachmentName() != null) lecture.setAttachmentName(request.attachmentName());
        if (request.durationMinutes() != null) lecture.setDurationMinutes(request.durationMinutes());
        if (request.order() != null) lecture.setOrder(request.order());
        if (request.isFreePreview() != null) lecture.setIsFreePreview(request.isFreePreview());
        if (request.resources() != null) lecture.setResources(request.resources());

        lecture.setUpdatedAt(LocalDateTime.now());

        TrainingLecture updatedLecture = trainingLectureRepository.save(lecture);
        return mapToLectureResponse(updatedLecture);
    }

    /**
     * Supprime une leçon
     */
    @Transactional
    public void deleteLecture(String lectureId) {
        log.info("Suppression de la leçon {}", lectureId);

        TrainingLecture lecture = trainingLectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Leçon introuvable", Map.of("lectureId", lectureId)));

        // Supprimer les données associées
        deleteLectureData(lectureId);

        // Supprimer la leçon
        trainingLectureRepository.delete(lecture);
        log.info("Leçon supprimée avec succès: {}", lectureId);
    }

    // ============ GESTION DE LA PROGRESSION ============

    /**
     * Met à jour la progression d'un étudiant pour une leçon
     */
    @Transactional
    public LectureProgressResponse updateProgress(String enrollmentId, String lectureId,
                                                  UpdateProgressRequest request) {
        log.info("Mise à jour de la progression pour l'inscription {} et la leçon {}",
                enrollmentId, lectureId);

        // Récupérer ou créer la progression
        LectureProgress progress = lectureProgressRepository
                .findByEnrollmentIdAndLectureId(enrollmentId, lectureId)
                .orElse(LectureProgress.builder()
                        .enrollmentId(enrollmentId)
                        .lectureId(lectureId)
                        .isCompleted(false)
                        .progressPercent(0)
                        .currentPosition(0)
                        .timeSpent(0)
                        .firstAccessedAt(LocalDateTime.now())
                        .build());

        // Mettre à jour les champs
        if (request.progressPercent() != null) {
            progress.setProgressPercent(request.progressPercent());
        }
        if (request.currentPosition() != null) {
            progress.setCurrentPosition(request.currentPosition());
        }
        if (request.timeSpent() != null) {
            progress.setTimeSpent(request.timeSpent());
        }
        if (request.isCompleted() != null && request.isCompleted()) {
            progress.markAsCompleted();
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());

        LectureProgress savedProgress = lectureProgressRepository.save(progress);
        return mapToProgressResponse(savedProgress);
    }

    /**
     * Récupère la progression d'un étudiant pour une offre
     */
    public List<LectureProgressResponse> getProgressByEnrollment(String enrollmentId) {
        log.info("Récupération de la progression pour l'inscription {}", enrollmentId);

        List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollmentId);
        return progressList.stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    // ============ MÉTHODES PRIVÉES ============

    private void deleteLectureData(String lectureId) {
        // Supprimer les progressions associées
        List<LectureProgress> progressList = lectureProgressRepository.findByLectureId(lectureId);
        lectureProgressRepository.deleteAll(progressList);

        // Supprimer les quiz associés
        List<TrainingQuiz> quizzes = trainingQuizRepository.findByParentIdAndParentType(
                lectureId, TrainingQuiz.ParentType.LECTURE);
        trainingQuizRepository.deleteAll(quizzes);
    }

    // ============ MÉTHODES DE MAPPING ============

    private TrainingSectionResponse mapToSectionResponse(TrainingSection section) {
        return new TrainingSectionResponse(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrder(),
                section.getDurationMinutes(),
                section.getIsFreePreview(),
                section.getCreatedAt()
        );
    }

    private TrainingLectureResponse mapToLectureResponse(TrainingLecture lecture) {
        return new TrainingLectureResponse(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getContent(),
                lecture.getType(),
                lecture.getVideoUrl(),
                lecture.getAttachmentUrl(),
                lecture.getAttachmentName(),
                lecture.getDurationMinutes(),
                lecture.getOrder(),
                lecture.getIsFreePreview(),
                lecture.getResources(),
                lecture.getCreatedAt()
        );
    }

    private LectureProgressResponse mapToProgressResponse(LectureProgress progress) {
        return new LectureProgressResponse(
                progress.getId(),
                progress.getEnrollmentId(),
                progress.getLectureId(),
                progress.getIsCompleted(),
                progress.getProgressPercent(),
                progress.getCurrentPosition(),
                progress.getTimeSpent(),
                progress.getLastAccessedAt(),
                progress.getCompletedAt()
        );
    }
    // Extension du TrainingContentService.java avec les méthodes manquantes

    /**
     * Récupère le contenu complet d'une offre avec progression optionnelle
     */
    public TrainingOfferContentResponse getOfferContent(String offerId, String studentId) {
        log.info("Récupération du contenu complet pour l'offre {} et l'étudiant {}", offerId, studentId);

        // Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Récupérer les sections avec leçons
        List<TrainingSection> sections = trainingSectionRepository.findByTrainingOfferIdOrderByOrder(offerId);
        List<TrainingSectionWithLectures> sectionsWithLectures = sections.stream()
                .map(section -> {
                    List<TrainingLecture> lectures = trainingLectureRepository.findBySectionIdOrderByOrder(section.getId());
                    List<TrainingLectureResponse> lectureResponses = lectures.stream()
                            .map(this::mapToLectureResponse)
                            .collect(Collectors.toList());
                    return new TrainingSectionWithLectures(
                            mapToSectionResponse(section),
                            lectureResponses
                    );
                })
                .collect(Collectors.toList());

        // Récupérer la progression si étudiant connecté
        StudentProgressSummary progressSummary = null;
        if (studentId != null) {
            progressSummary = calculateStudentProgress(studentId, offerId);
        }

        // Calculer les statistiques
        OfferStatisticsResponse statistics = calculateOfferStats(offerId);

        // Informations de base de l'offre
        OfferBasicInfo offerInfo = new OfferBasicInfo(
                offer.getId(),
                offer.getLabel(),
                offer.getOfferType().name(),
                offer.getTuitionAmount(),
                offer.getCurrency(),
                offer.getIsPublished(),
                offer.getIsApproved()
        );

        return new TrainingOfferContentResponse(
                offerInfo,
                sectionsWithLectures,
                progressSummary,
                statistics
        );
    }

    /**
     * Publie ou dépublie une offre
     */
    @Transactional
    public PublishResponse publishOffer(String offerId, PublishOfferRequest request) {
        log.info("Publication de l'offre {}: {}", offerId, request.isPublished());

        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifier que l'offre a au moins une section avec des leçons
        if (request.isPublished()) {
            List<TrainingSection> sections = trainingSectionRepository.findByTrainingOfferIdOrderByOrder(offerId);
            if (sections.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT,
                        "L'offre doit avoir au moins une section avant d'être publiée");
            }

            for (TrainingSection section : sections) {
                List<TrainingLecture> lectures = trainingLectureRepository.findBySectionIdOrderByOrder(section.getId());
                if (lectures.isEmpty()) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT,
                            "Chaque section doit avoir au moins une leçon avant de publier l'offre");
                }
            }
        }

        offer.setIsPublished(request.isPublished());
        offer.setUpdatedAt(LocalDateTime.now());

        trainingOfferRepository.save(offer);

        String message = request.isPublished() ?
                "Offre publiée avec succès. Elle est maintenant en attente d'approbation." :
                "Offre dépubliée avec succès.";

        return new PublishResponse(
                offerId,
                request.isPublished(),
                message
        );
    }

    /**
     * Approuve ou rejette une offre (admin uniquement)
     */
    @Transactional
    public ApprovalResponse approveOffer(String offerId, ApprovalRequest request) {
        log.info("Approbation de l'offre {} : {}", offerId, request.isApproved());

        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (request.isApproved()) {
            offer.setIsApproved(true);
            offer.setApprovalDate(LocalDateTime.now());
        } else {
            offer.setIsApproved(false);
            offer.setApprovalDate(null);
            offer.setIsPublished(false); // Dépublier si rejeté
        }

        offer.setUpdatedAt(LocalDateTime.now());
        trainingOfferRepository.save(offer);

        String message = request.isApproved() ?
                "L'offre a été approuvée et est maintenant disponible pour inscription" :
                "L'offre a été rejetée";

        return new ApprovalResponse(
                offerId,
                request.isApproved(),
                request.isApproved() ? offer.getApprovalDate() : null,
                message
        );
    }

    /**
     * Calcule les statistiques d'une offre
     */
    public OfferStatisticsResponse calculateOfferStats(String offerId) {
        log.info("Calcul des statistiques pour l'offre {}", offerId);

        // Compter les étudiants inscrits
        long totalStudents = enrollmentRepository.countByProgramLevelId(offerId);

        // Calculer la note moyenne des avis
        List<Review> reviews = reviewRepository.findByTrainingOfferId(offerId);
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Calculer le taux de complétion moyen
        double completionRate = calculateCompletionRate(offerId);

        // Calculer les revenus générés
        BigDecimal totalRevenue = calculateTotalRevenue(offerId);

        return new OfferStatisticsResponse(
                (int) totalStudents,
                averageRating,
                reviews.size(),
                completionRate,
                totalRevenue
        );
    }

    /**
     * Ajoute un document à une offre
     */
    @Transactional
    public DocumentResponse addDocument(String offerId, CreateDocumentRequest request) {
        log.info("Ajout d'un document à l'offre {}: {}", offerId, request.title());

        // Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Créer le document (utilise le modèle Document existant)
        // Logique à implémenter selon votre modèle Document

        return new DocumentResponse(
                UUID.randomUUID().toString(),
                request.title(),
                request.fileUrl(),
                request.fileSize(),
                request.mimeType(),
                request.isPublic(),
                0
        );
    }

    /**
     * Récupère les documents d'une offre
     */
    public List<DocumentResponse> getDocuments(String offerId) {
        log.info("Récupération des documents pour l'offre {}", offerId);

        // Logique à implémenter selon votre modèle Document
        return new ArrayList<>();
    }

    /**
     * Permet le téléchargement d'un document
     */
    public ResponseEntity<Resource> downloadDocument(String documentId) {
        log.info("Téléchargement du document {}", documentId);

        // Logique à implémenter selon votre modèle Document
        return ResponseEntity.notFound().build();
    }

    /**
     * Tableau de bord de l'institution
     */
    public InstitutionDashboardResponse getInstitutionDashboard(String institutionId) {
        log.info("Récupération du tableau de bord pour l'institution {}", institutionId);

        // Compter les offres
        int totalOffers = (int) trainingOfferRepository.countByInstitutionId(institutionId);

        List<TrainingOffer> offers = trainingOfferRepository.findByInstitutionId(institutionId);
        int publishedOffers = (int) offers.stream().filter(TrainingOffer::getIsPublished).count();
        int approvedOffers = (int) offers.stream()
                .filter(offer -> offer.getIsPublished() && offer.getIsApproved()).count();

        // Compter les étudiants inscrits
        int totalStudents = offers.stream()
                .mapToInt(offer -> (int) enrollmentRepository.countByProgramLevelId(offer.getId()))
                .sum();

        // Inscriptions récentes
        List<RecentEnrollment> recentEnrollments = getRecentEnrollments(institutionId);

        // Avis récents
        List<RecentReview> recentReviews = getRecentReviews(institutionId);

        // Revenus du mois
        BigDecimal monthlyRevenue = calculateMonthlyRevenue(institutionId);

        return new InstitutionDashboardResponse(
                totalOffers,
                publishedOffers,
                approvedOffers,
                totalStudents,
                recentEnrollments,
                recentReviews,
                monthlyRevenue
        );
    }

    /**
     * Tableau de bord de l'étudiant
     */
    public StudentDashboardResponse getStudentDashboard(String studentId) {
        log.info("Récupération du tableau de bord pour l'étudiant {}", studentId);

        // Compter les inscriptions
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        int totalEnrollments = enrollments.size();

        // Cours récents avec progression
        List<CourseWithProgress> recentCourses = enrollments.stream()
                .map(enrollment -> {
                    TrainingOffer offer = trainingOfferRepository.findById(enrollment.getProgramLevelId())
                            .orElse(null);
                    if (offer != null) {
                        double progress = calculateStudentProgressForOffer(studentId, offer.getId());
                        return new CourseWithProgress(
                                offer.getId(),
                                offer.getLabel(),
                                progress,
                                enrollment.getUpdatedAt()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());

        // Progression globale moyenne
        double overallProgress = recentCourses.stream()
                .mapToDouble(CourseWithProgress::progress)
                .average()
                .orElse(0.0);

        // Certificats obtenus
        List<CertificateInfo> certificates = getCertificatesForStudent(studentId);

        return new StudentDashboardResponse(
                totalEnrollments,
                recentCourses,
                overallProgress,
                certificates
        );
    }

    /**
     * Génère un rapport CSV des étudiants
     */
    public ResponseEntity<Resource> generateStudentReport(String institutionId) {
        log.info("Génération du rapport des étudiants pour l'institution {}", institutionId);

        // Récupérer les données
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // À adapter

        // Générer le CSV
        String csvContent = generateStudentCSV(enrollments);

        ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body((Resource) resource);
    }

    /**
     * Génère un rapport de progression pour une offre
     */
    public ResponseEntity<Resource> generateProgressReport(String offerId) {
        log.info("Génération du rapport de progression pour l'offre {}", offerId);

        // Récupérer les données de progression
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // À adapter

        // Générer le CSV
        String csvContent = generateProgressCSV(enrollments, offerId);

        ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=progress_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body((Resource) resource);
    }

// ============ MÉTHODES PRIVÉES UTILITAIRES ============

    private StudentProgressSummary calculateStudentProgress(String studentId, String offerId) {
        // Logique de calcul de progression
        return new StudentProgressSummary(0.0, 0, 0, null);
    }

    private double calculateCompletionRate(String offerId) {
        // Logique de calcul du taux de complétion
        return 0.0;
    }

    private BigDecimal calculateTotalRevenue(String offerId) {
        // Logique de calcul des revenus
        return BigDecimal.ZERO;
    }

    private List<RecentEnrollment> getRecentEnrollments(String institutionId) {
        // Logique pour récupérer les inscriptions récentes
        return new ArrayList<>();
    }

    private List<RecentReview> getRecentReviews(String institutionId) {
        // Logique pour récupérer les avis récents
        return new ArrayList<>();
    }

    private BigDecimal calculateMonthlyRevenue(String institutionId) {
        // Logique pour calculer les revenus du mois
        return BigDecimal.ZERO;
    }

    private double calculateStudentProgressForOffer(String studentId, String offerId) {
        // Logique pour calculer la progression d'un étudiant pour une offre
        return 0.0;
    }

    private List<CertificateInfo> getCertificatesForStudent(String studentId) {
        // Logique pour récupérer les certificats d'un étudiant
        return new ArrayList<>();
    }

    private String generateStudentCSV(List<Enrollment> enrollments) {
        // Logique de génération du CSV des étudiants
        return "CSV Content";
    }

    private String generateProgressCSV(List<Enrollment> enrollments, String offerId) {
        // Logique de génération du CSV de progression
        return "CSV Content";
    }
}