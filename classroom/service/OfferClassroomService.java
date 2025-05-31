package com.payiskoul.institution.classroom.service;

import com.payiskoul.institution.classroom.dto.ClassroomResponse;
import com.payiskoul.institution.classroom.dto.CreateClassroomRequest;
import com.payiskoul.institution.classroom.dto.UpdateClassroomRequest;
import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.OfferType;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferClassroomService {

    private final ClassroomRepository classroomRepository;
    private final TrainingOfferRepository trainingOfferRepository;

    /**
     * Crée une classe pour une offre académique
     */
    @Transactional
    public ClassroomResponse createClassroomForOffer(String institutionId, String offerId,
                                                     CreateClassroomRequest request) {
        log.info("Création d'une classe pour l'offre {} de l'institution {}: {}",
                offerId, institutionId, request.name());

        // Vérifier que l'offre existe et appartient à l'institution
        TrainingOffer offer = validateOfferAndInstitution(institutionId, offerId);

        // Vérifier que c'est une offre académique
        if (offer.getOfferType() != OfferType.ACADEMIC) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Les classes ne peuvent être créées que pour les offres académiques",
                    Map.of("offerType", offer.getOfferType().name()));
        }

        // Vérifier qu'une classe avec ce nom n'existe pas déjà pour cette offre
        Optional<Classroom> existingClassroom = classroomRepository.findByNameAndProgramLevelId(
                request.name(), offerId);
        if (existingClassroom.isPresent()) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS,
                    "Une classe avec ce nom existe déjà pour cette offre",
                    Map.of("name", request.name(), "offerId", offerId));
        }

        // Créer la classe
        Classroom classroom = Classroom.builder()
                .name(request.name())
                .capacity(request.capacity())
                .currentCount(0)
                .programLevelId(offerId) // L'offre devient le niveau de programme
                .institutionId(institutionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Classe créée avec succès: {}", savedClassroom.getId());

        return mapToClassroomResponse(savedClassroom);
    }

    /**
     * Met à jour une classe d'une offre
     */
    @Transactional
    public ClassroomResponse updateClassroomForOffer(String institutionId, String offerId,
                                                     String classroomId, UpdateClassroomRequest request) {
        log.info("Mise à jour de la classe {} pour l'offre {} de l'institution {}",
                classroomId, offerId, institutionId);

        // Vérifier que l'offre existe et appartient à l'institution
        validateOfferAndInstitution(institutionId, offerId);

        // Récupérer la classe
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                        "Classe introuvable", Map.of("classroomId", classroomId)));

        // Vérifier que la classe appartient à cette offre
        if (!classroom.getProgramLevelId().equals(offerId)) {
            throw new BusinessException(ErrorCode.INVALID_CLASSROOM_PROGRAM,
                    "Cette classe n'appartient pas à cette offre",
                    Map.of("classroomId", classroomId, "offerId", offerId));
        }

        // Vérifier que la nouvelle capacité n'est pas inférieure au nombre d'étudiants actuels
        if (request.capacity() < classroom.getCurrentCount()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "La capacité ne peut pas être inférieure au nombre d'étudiants actuels",
                    Map.of(
                            "requestedCapacity", request.capacity(),
                            "currentCount", classroom.getCurrentCount()
                    ));
        }

        // Vérifier l'unicité du nom si il a changé
        if (!classroom.getName().equals(request.name())) {
            Optional<Classroom> existingClassroom = classroomRepository.findByNameAndProgramLevelId(
                    request.name(), offerId);
            if (existingClassroom.isPresent()) {
                throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS,
                        "Une classe avec ce nom existe déjà pour cette offre",
                        Map.of("name", request.name(), "offerId", offerId));
            }
        }

        // Mettre à jour les champs
        classroom.setName(request.name());
        classroom.setCapacity(request.capacity());
        classroom.setUpdatedAt(LocalDateTime.now());

        Classroom updatedClassroom = classroomRepository.save(classroom);
        log.info("Classe mise à jour avec succès: {}", updatedClassroom.getId());

        return mapToClassroomResponse(updatedClassroom);
    }

    /**
     * Supprime une classe d'une offre
     */
    @Transactional
    public void deleteClassroomForOffer(String institutionId, String offerId, String classroomId) {
        log.info("Suppression de la classe {} pour l'offre {} de l'institution {}",
                classroomId, offerId, institutionId);

        // Vérifier que l'offre existe et appartient à l'institution
        validateOfferAndInstitution(institutionId, offerId);

        // Récupérer la classe
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                        "Classe introuvable", Map.of("classroomId", classroomId)));

        // Vérifier que la classe appartient à cette offre
        if (!classroom.getProgramLevelId().equals(offerId)) {
            throw new BusinessException(ErrorCode.INVALID_CLASSROOM_PROGRAM,
                    "Cette classe n'appartient pas à cette offre",
                    Map.of("classroomId", classroomId, "offerId", offerId));
        }

        // Vérifier que la classe est vide
        if (classroom.getCurrentCount() > 0) {
            throw new BusinessException(ErrorCode.CLASSROOM_FULL,
                    "Impossible de supprimer une classe qui contient des étudiants",
                    Map.of(
                            "classroomId", classroomId,
                            "currentCount", classroom.getCurrentCount()
                    ));
        }

        // Supprimer la classe
        classroomRepository.delete(classroom);
        log.info("Classe supprimée avec succès: {}", classroomId);
    }

    // ============ MÉTHODES PRIVÉES ============

    /**
     * Valide qu'une offre existe et appartient à l'institution
     */
    private TrainingOffer validateOfferAndInstitution(String institutionId, String offerId) {
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        return offer;
    }

    /**
     * Convertit une entité Classroom en DTO ClassroomResponse
     */
    private ClassroomResponse mapToClassroomResponse(Classroom classroom) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getCapacity(),
                classroom.getCurrentCount(),
                classroom.getCreatedAt().toLocalDate()
        );
    }
}