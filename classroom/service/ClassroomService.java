package com.payiskoul.institution.classroom.service;

import com.payiskoul.institution.classroom.dto.ClassroomList;
import com.payiskoul.institution.classroom.dto.ClassroomResponse;
import com.payiskoul.institution.classroom.dto.CreateClassroomRequest;
import com.payiskoul.institution.classroom.dto.ProgramInfo;
import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.ProgramLevelNotFoundException;
import com.payiskoul.institution.program.model.ProgramLevel;
import com.payiskoul.institution.program.repository.ProgramLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ProgramLevelRepository programLevelRepository;

    /**
     * Crée une nouvelle classe pour un programme spécifique
     * @param institutionId ID de l'institution
     * @param programId ID du programme
     * @param request données de la classe à créer
     * @return les informations de la classe créée
     */
    @Transactional
    @CacheEvict(value = "classrooms", key = "{#institutionId, #programId}")
    public ClassroomResponse createClassroom(String institutionId, String programId, CreateClassroomRequest request) {
        log.info("Création d'une nouvelle classe pour l'institution {} et le programme {}: {}",
                institutionId, programId, request.name());

        // Vérifier que le programme existe et appartient à l'institution
        ProgramLevel program = programLevelRepository.findById(programId)
                .orElseThrow(() -> new ProgramLevelNotFoundException("Programme introuvable",
                        Map.of("programId", programId)));

        if (!program.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Le programme spécifié n'appartient pas à cette institution",
                    Map.of(
                            "institutionId", institutionId,
                            "programId", programId,
                            "programInstitutionId", program.getInstitutionId()
                    ));
        }

        // Vérifier qu'une classe avec ce nom n'existe pas déjà pour ce programme
        Optional<Classroom> existingClassroom = classroomRepository.findByNameAndProgramLevelId(request.name(), programId);
        if (existingClassroom.isPresent()) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS,
                    "Une classe avec ce nom existe déjà pour ce programme",
                    Map.of(
                            "name", request.name(),
                            "programId", programId
                    ));
        }

        // Créer la classe
        Classroom classroom = Classroom.builder()
                .name(request.name())
                .capacity(request.capacity())
                .currentCount(0) // Aucun étudiant au départ
                .programLevelId(programId)
                .institutionId(institutionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Sauvegarder la classe
        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Classe créée avec succès: {}", savedClassroom.getId());

        // Retourner la réponse
        return mapToClassroomResponse(savedClassroom, program.getName());
    }

    /**
     * Récupère toutes les classes pour un programme spécifique
     * @param institutionId ID de l'institution
     * @param programId ID du programme
     * @return la liste des classes
     */
    //@Cacheable(value = "classrooms", key = "{#institutionId, #programId}")
    public ClassroomList getClassroomsByProgram(String institutionId, String programId) {
        log.info("Récupération des classes pour l'institution {} et le programme {}", institutionId, programId);

        // Vérifier que le programme existe
        ProgramLevel program = programLevelRepository.findById(programId)
                .orElseThrow(() -> new ProgramLevelNotFoundException("Programme introuvable",
                        Map.of("programId", programId)));

        List<Classroom> classrooms = classroomRepository.findByInstitutionIdAndProgramLevelId(institutionId, programId);
        var totalStudents = classrooms.stream()
                .mapToInt(Classroom::getCurrentCount)
                .sum();
        var programInfo = new ProgramInfo(program.getId(), program.getName(),program.getAcademicYear()
                , program.getDuration(), program.getDurationUnit(), totalStudents);
        return new ClassroomList(programInfo, classrooms.stream()
                .map(classroom -> mapToClassroomResponse(classroom, program.getName()))
                .toList());
    }

    /**
     * Ajoute un étudiant à une classe disponible
     * @param programId ID du programme
     * @param classroomId ID de la classe (optionnel)
     * @return l'ID de la classe où l'étudiant a été ajouté
     * @throws BusinessException si aucune classe n'a de place disponible
     */
    /**
     * Ajoute un étudiant à une classe disponible
     * @param programId ID du programme
     * @param classroomId ID de la classe (optionnel)
     * @return l'ID de la classe où l'étudiant a été ajouté
     * @throws BusinessException si aucune classe n'a de place disponible
     */
    @Transactional
    public String addStudentToClassroom(String programId, String classroomId) {
        log.info("Ajout d'un étudiant à une classe pour le programme {}", programId);

        Classroom classroom;

        // Si un ID de classe est spécifié, utiliser cette classe
        if (classroomId != null && !classroomId.isEmpty()) {
            classroom = classroomRepository.findById(classroomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND,
                            "Classe introuvable", Map.of("classroomId", classroomId)));

            // Vérifier que la classe appartient au bon programme
            if (!classroom.getProgramLevelId().equals(programId)) {
                throw new BusinessException(ErrorCode.INVALID_CLASSROOM_PROGRAM,
                        "La classe spécifiée n'appartient pas à ce programme",
                        Map.of(
                                "classroomId", classroomId,
                                "programId", programId,
                                "classroomProgramId", classroom.getProgramLevelId()
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
                            programId, Integer.MAX_VALUE)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NO_CLASSROOM_AVAILABLE,
                            "Aucune classe disponible pour ce programme",
                            Map.of("programId", programId)));
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
     * @param programName nom du programme
     * @return DTO ClassroomResponse
     */
    private ClassroomResponse mapToClassroomResponse(Classroom classroom, String programName) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getCapacity(),
                classroom.getCurrentCount(),
                classroom.getCreatedAt().toLocalDate()
        );
    }
}