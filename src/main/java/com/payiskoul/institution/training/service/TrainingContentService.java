package com.payiskoul.institution.training.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.training.dto.*;
import com.payiskoul.institution.training.model.LectureProgress;
import com.payiskoul.institution.training.model.TrainingLecture;
import com.payiskoul.institution.training.model.TrainingQuiz;
import com.payiskoul.institution.training.model.TrainingSection;
import com.payiskoul.institution.training.repository.LectureProgressRepository;
import com.payiskoul.institution.training.repository.TrainingLectureRepository;
import com.payiskoul.institution.training.repository.TrainingQuizRepository;
import com.payiskoul.institution.training.repository.TrainingSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
}