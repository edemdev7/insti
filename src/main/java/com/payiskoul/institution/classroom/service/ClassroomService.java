package com.payiskoul.institution.classroom.service;

import com.payiskoul.institution.classroom.dto.ClassroomList;
import com.payiskoul.institution.classroom.dto.ClassroomResponse;
import com.payiskoul.institution.classroom.dto.CreateClassroomRequest;
import com.payiskoul.institution.classroom.dto.ProgramInfo;
import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service de classes mis à jour pour utiliser le modèle unifié TrainingOffer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final TrainingOfferRepository trainingOfferRepository; // Remplace ProgramLevelRepository

    /**
     * Crée une nouvelle classe pour une offre spécifique
     * @param institutionId ID de l'institution
     * @param offerId ID de l'offre (remplace programId)
     * @param request données de la classe à créer
     * @return les informations de la classe créée
     */
    @Transactional
    @CacheEvict(value = "classrooms", key = "{#institutionId, #offerId}")
    public ClassroomResponse createClassroom(String institutionId, String offerId, CreateClassroomRequest request) {
        log.info("Création d'une nouvelle classe pour l'institution {} et l'offre {}: {}",
                institutionId, offerId, request.name());

        // Vérifier que l'offre existe et appartient à l'institution
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "L'offre spécifiée n'appartient pas à cette institution",
                    Map.of(
                            "institutionId", institutionId,
                            "offerId", offerId,
                            "offerInstitutionId", offer.getInstitutionId()
                    ));
        }

        // Vérifier qu'une classe avec ce nom n'existe pas déjà pour cette offre
        Optional<Classroom> existingClassroom = classroomRepository.findByNameAndProgramLevelId(request.name(), offerId);
        if (existingClassroom.isPresent()) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS,
                    "Une classe avec ce nom existe déjà pour cette offre",
                    Map.of(
                            "name", request.name(),
                            "offerId", offerId
                    ));
        }

        // Créer la classe
        Classroom classroom = Classroom.builder()
                .name(request.name())
                .capacity(request.capacity())
                .currentCount(0) // Aucun étudiant au départ
                .programLevelId(offerId) // Garde le même nom de champ pour compatibilité DB
                .institutionId(institutionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Sauvegarder la classe
        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Classe créée avec succès: {}", savedClassroom.getId());

        // Retourner la réponse
        return mapToClassroomResponse(savedClassroom);
    }

    /**
     * Méthode de compatibilité - garde l'ancien nom mais utilise la nouvelle logique
     */
    @Transactional
    @CacheEvict(value = "classrooms", key = "{#institutionId, #programId}")
    public ClassroomResponse createClassroomForProgram(String institutionId, String programId, CreateClassroomRequest request) {
        log.warn("Utilisation de la méthode dépréciée createClassroomForProgram - migrer vers createClassroom");
        return createClassroom(institutionId, programId, request);
    }

    /**
     * Récupère toutes les classes pour une offre spécifique
     * @param institutionId ID de l'institution
     * @param offerId ID de l'offre (remplace programId)
     * @return la liste des classes
     */
    public ClassroomList getClassroomsByOffer(String institutionId, String offerId) {
        log.info("Récupération des classes pour l'institution {} et l'offre {}", institutionId, offerId);

        // Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        List<Classroom> classrooms = classroomRepository.findByInstitutionIdAndProgramLevelId(institutionId, offerId);
        var totalStudents = classrooms.stream()
                .mapToInt(Classroom::getCurrentCount)
                .sum();

        var programInfo = new ProgramInfo(
                offer.getId(),
                offer.getLabel(),
                offer.getAcademicYear(),
                offer.getDuration(),
                offer.getDurationUnit(),
                totalStudents
        );

        return new ClassroomList(programInfo, classrooms.stream()
                .map(this::mapToClassroomResponse)
                .toList());
    }

    /**
     * Méthode de compatibilité
     */
    public ClassroomList getClassroomsByProgram(String institutionId, String programId) {
        log.warn("Utilisation de la méthode dépréciée getClassroomsByProgram - migrer vers getClassroomsByOffer");
        return getClassroomsByOffer(institutionId, programId);
    }

    /**
     * Ajoute un étudiant à une classe disponible
     * @param offerId ID de l'offre (remplace programId)
     * @param classroomId ID de la classe (optionnel)
     * @return l'ID de la classe dans l'étudiant a été ajouté
     * @throws BusinessException si aucune classe n'a de place disponible
     */
    @Transactional
    public String addStudentToClassroom(String offerId, String classroomId) {
        log.info("Ajout d'un étudiant à une classe pour l'offre {}", offerId);

        Classroom classroom;

        // Si un ID de classe est spécifié, utiliser cette classe
        if (classroomId != null && !classroomId.isEmpty()) {
            classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                            "Classe introuvable", Map.of("classroomId", classroomId)));

            // Vérifier que la classe appartient à la bonne offre
            if (!classroom.getProgramLevelId().equals(offerId)) {
                throw new BusinessException(ErrorCode.INVALID_CLASSROOM_PROGRAM,
                        "La classe spécifiée n'appartient pas à cette offre",
                        Map.of(
                                "classroomId", classroomId,
                                "offerId", offerId,
                                "classroomOfferId", classroom.getProgramLevelId()
                        ));
            }

            // Vérifier qu'il reste de la place
            if (classroom.getCurrentCount() >= classroom.getCapacity()) {
                throw new BusinessException(ErrorCode.CLASSROOM_FULL,
                        "La classe spécifiée est complète",
                        Map.of(
                                "classroomId", classroomId,
                                "capacity", classroom.getCapacity(),
                                "currentCount", classroom.getCurrentCount()
                        ));
            }
        } else {
            // Sinon, trouver une classe avec de la place disponible
            classroom = classroomRepository.findFirstByProgramLevelIdAndCurrentCountLessThan(
                            offerId, Integer.MAX_VALUE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NO_CLASSROOM_AVAILABLE,
                            "Aucune classe disponible pour cette offre",
                            Map.of("offerId", offerId)));
        }

        // Incrémenter le nombre d'étudiants
        classroom.setCurrentCount(classroom.getCurrentCount() + 1);
        classroomRepository.save(classroom);

        log.info("Étudiant ajouté à la classe {}", classroom.getId());
        return classroom.getId();
    }

    /**
     * Supprime un étudiant d'une classe
     * @param classroomId ID de la classe
     */
    @Transactional
    public void removeStudentFromClassroom(String classroomId) {
        log.info("Suppression d'un étudiant de la classe {}", classroomId);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                        "Classe introuvable", Map.of("classroomId", classroomId)));

        if (classroom.getCurrentCount() > 0) {
            classroom.setCurrentCount(classroom.getCurrentCount() - 1);
            classroomRepository.save(classroom);
            log.info("Étudiant retiré de la classe {}", classroomId);
        } else {
            log.warn("Tentative de retirer un étudiant d'une classe vide: {}", classroomId);
        }
    }

    /**
     * Convertit une entité Classroom en DTO ClassroomResponse
     * @param classroom entité Classroom
     * @return DTO ClassroomResponse
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