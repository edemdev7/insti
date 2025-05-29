package com.payiskoul.institution.student.service;

import com.payiskoul.institution.classroom.service.ClassroomService;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.ProgramLevelNotFoundException;
import com.payiskoul.institution.exception.StudentAlreadyExistsException;
import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import com.payiskoul.institution.program.model.ProgramLevel;
import com.payiskoul.institution.program.repository.ProgramLevelRepository;
import com.payiskoul.institution.student.dto.CreateStudentRequest;
import com.payiskoul.institution.student.dto.StudentResponse;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final ProgramLevelRepository programLevelRepository;
    private final InstitutionRepository institutionRepository;
    private final MatriculeGenerator matriculeGenerator;
    private final EnrollmentService enrollmentService;
    private final ClassroomService classroomService;

    /**
     * Crée un nouvel étudiant et l'inscrit au programme spécifié
     * @param request les données de l'étudiant
     * @return les informations de l'étudiant créé
     */
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        log.info("Création d'un nouvel étudiant: {}", request.fullName());

        // Vérifier si l'étudiant existe déjà (par email)
        if (studentRepository.existsByEmail(request.email())) {
            throw new StudentAlreadyExistsException("Un étudiant avec cet email existe déjà",
                    Map.of("email", request.email()));
        }

        // Vérifier si le programme existe
        ProgramLevel programLevel = programLevelRepository.findById(request.programId())
                .orElseThrow(() -> new ProgramLevelNotFoundException("Le programme spécifié n'existe pas",
                        Map.of("programId", request.programId())));

        // Récupérer l'institution associée au programme
        Institution institution = institutionRepository.findById(programLevel.getInstitutionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSTITUTION_NOT_FOUND,
                        "Institution introuvable pour le programme spécifié",
                        Map.of("institutionId", programLevel.getInstitutionId())));

        // Récupérer le code du pays de l'institution
        String countryCode = institution.getAddress().getCountry();

        // Utiliser un code par défaut si celui de l'institution n'est pas disponible
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = "CI"; // Code par défaut pour la Côte d'Ivoire
            log.warn("Code pays non trouvé pour l'institution {}, utilisation du code par défaut: {}",
                    institution.getId(), countryCode);
        }

        // Générer un matricule unique avec le code pays de l'institution
        String matricule = matriculeGenerator.generateMatricule(countryCode);

        // Créer l'étudiant
        Student student = Student.builder()
                .matricule(matricule)
                .fullName(request.fullName())
                .gender(request.gender())
                .birthDate(request.birthDate())
                .email(request.email())
                .phone(request.phone())
                .registeredAt(LocalDateTime.now())
                .build();

        // Sauvegarder l'étudiant
        Student savedStudent = studentRepository.save(student);
        log.info("Étudiant créé avec succès. Matricule: {}", savedStudent.getMatricule());

        // Assignation à une classe spécifique ou automatique
        String classroomId = null;
        try {
            // Utiliser la classe spécifiée ou en trouver une automatiquement
            classroomId = classroomService.addStudentToClassroom(programLevel.getId(), request.classroomId());
            log.info("Étudiant assigné à la classe: {}", classroomId);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.NO_CLASSROOM_AVAILABLE) {
                log.warn("Aucune classe disponible pour ce programme: {}", programLevel.getId());
            } else if (e.getErrorCode() == ErrorCode.CLASSROOM_FULL) {
                log.warn("La classe spécifiée est complète: {}", request.classroomId());
            } else {
                throw e;
            }
        }

        // Inscrire l'étudiant au programme spécifié
        Enrollment enrollment = enrollmentService.enrollStudentToProgram(
                savedStudent.getId(),
                programLevel.getId(),
                programLevel.getInstitutionId(),
                programLevel.getAcademicYear(),
                classroomId);

        log.info("Étudiant inscrit avec succès au programme. Enrollment ID: {}", enrollment.getId());

        // Retourner la réponse
        return mapToStudentResponse(savedStudent, programLevel.getId());
    }

    /**
     * Récupérer les informations d'un étudiant par son matricule
     * @param matricule le matricule de l'étudiant
     * @return les informations de l'étudiant
     */
    @Cacheable(value = "students", key = "#matricule")
    public StudentResponse getStudentByMatricule(String matricule) {
        log.info("Récupération de l'étudiant avec le matricule: {}", matricule);

        // Rechercher l'étudiant
        Student student = studentRepository.findByMatricule(matricule)
                .orElseThrow(() -> new StudentNotFoundException("Aucun étudiant trouvé avec ce matricule",
                        Map.of("matricule", matricule)));

        String programId = enrollmentService.getLatestProgramIdForStudent(student.getId());

        return mapToStudentResponse(student, programId);
    }

    /**
     * Supprime un étudiant par son matricule
     * @param matricule le matricule de l'étudiant
     */
    @CacheEvict(value = "students", key = "#matricule")
    @Transactional
    public void deleteStudentByMatricule(String matricule) {
        log.info("Suppression de l'étudiant avec le matricule: {}", matricule);

        // Rechercher l'étudiant
        Student student = studentRepository.findByMatricule(matricule)
                .orElseThrow(() -> new StudentNotFoundException("Aucun étudiant trouvé avec ce matricule",
                        Map.of("matricule", matricule)));

        // Supprimer l'étudiant
        studentRepository.delete(student);
        log.info("Étudiant supprimé avec succès");
    }

    /**
     * Convertit une entité Student en DTO StudentResponse
     * @param student l'entité Student
     * @param programId l'ID du programme
     * @return le DTO StudentResponse
     */
    private StudentResponse mapToStudentResponse(Student student, String programId) {
        return new StudentResponse(
                student.getId(),
                student.getMatricule(),
                student.getFullName(),
                student.getGender(),
                student.getBirthDate(),
                student.getEmail(),
                student.getPhone(),
                student.getRegisteredAt(),
                programId
        );
    }
}