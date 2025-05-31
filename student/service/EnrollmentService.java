package com.payiskoul.institution.student.service;

import com.payiskoul.institution.classroom.service.ClassroomService;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.EnrollmentAlreadyExistsException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.ProgramLevelNotFoundException;
import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.ProgramLevelRepository;
import com.payiskoul.institution.student.dto.CreateEnrollmentRequest;
import com.payiskoul.institution.student.dto.EnrollmentResponse;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Enrollment.EnrollmentStatus;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.tuition.service.TuitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ProgramLevelRepository programLevelRepository;
    private final TuitionService tuitionService;
    private final ClassroomService classroomService;

    /**
     * Inscrit un étudiant à un programme
     * @param request données de l'inscription
     * @return les informations de l'inscription
     */
    @Transactional
    public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request) {
        log.info("Création d'une nouvelle inscription pour l'étudiant {}", request.studentId());

        // Vérifier si l'étudiant existe
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new StudentNotFoundException("L'étudiant spécifié n'existe pas",
                        Map.of("studentId", request.studentId())));

        // Vérifier si le programme existe
        ProgramLevel programLevel = programLevelRepository.findById(request.programId())
                .orElseThrow(() -> new ProgramLevelNotFoundException("Le programme spécifié n'existe pas",
                        Map.of("programId", request.programId())));

        // Vérifier si l'étudiant est déjà inscrit à ce programme pour cette année académique
        if (enrollmentRepository.existsByStudentIdAndProgramLevelIdAndAcademicYear(
                request.studentId(), request.programId(), request.academicYear())) {
            throw new EnrollmentAlreadyExistsException(
                    "L'étudiant est déjà inscrit à ce programme pour cette année académique",
                    Map.of(
                            "studentId", request.studentId(),
                            "programId", request.programId(),
                            "academicYear", request.academicYear()
                    ));
        }

        // Assigner l'étudiant à une classe spécifique ou automatiquement
        String classroomId = null;
        try {
            // Utiliser la classe spécifiée ou en trouver une automatiquement
            classroomId = classroomService.addStudentToClassroom(request.programId(), request.classroomId());
            log.info("Étudiant assigné à la classe: {}", classroomId);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.NO_CLASSROOM_AVAILABLE) {
                log.warn("Aucune classe disponible pour ce programme: {}", request.programId());
            } else if (e.getErrorCode() == ErrorCode.CLASSROOM_FULL) {
                log.warn("La classe spécifiée est complète: {}", request.classroomId());
            } else {
                throw e;
            }
        }

        // Créer l'inscription
        Enrollment enrollment = enrollStudentToProgram(
                request.studentId(),
                request.programId(),
                programLevel.getInstitutionId(),
                request.academicYear(),
                classroomId
        );

        // Retourner la réponse
        return mapToEnrollmentResponse(enrollment);
    }

    /**
     * Inscrit un étudiant à un programme
     * @param studentId ID de l'étudiant
     * @param programLevelId ID du programme
     * @param institutionId ID de l'institution
     * @param academicYear année académique
     * @param classroomId ID de la classe (optionnel)
     * @return l'inscription créée
     */
    @Transactional
    public Enrollment enrollStudentToProgram(
            String studentId,
            String programLevelId,
            String institutionId,
            String academicYear,
            String classroomId) {

        // Créer l'inscription
        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .programLevelId(programLevelId)
                .institutionId(institutionId)
                .classroomId(classroomId)  // Peut être null
                .academicYear(academicYear)
                .status(EnrollmentStatus.ENROLLED)
                .enrolledAt(LocalDateTime.now())
                .build();

        // Sauvegarder l'inscription
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscription créée avec succès. ID: {}", savedEnrollment.getId());

//        // Envoyer une notification
//        notificationService.sendEnrollmentNotification(
//                savedEnrollment.getId(),
//                savedEnrollment.getStudentId(),
//                savedEnrollment.getProgramLevelId()
//        );

        // Récupérer le programme pour les frais de scolarité
        ProgramLevel programLevel = programLevelRepository.findById(programLevelId)
                .orElseThrow(() -> new ProgramLevelNotFoundException("Programme introuvable",
                        Map.of("programId", programLevelId)));

        // Récupérer l'étudiant pour le matricule
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                        Map.of("studentId", studentId)));

        // Créer un statut de paiement pour cette inscription
        if (programLevel.getTuition() != null) {
            tuitionService.createTuitionStatus(
                    savedEnrollment.getId(),
                    studentId,
                    student.getMatricule(),
                    programLevel.getTuition().getAmount(),
                    programLevel.getTuition().getCurrency(),
                    BigDecimal.ZERO  // Montant payé initial à 0
            );
        }

        return savedEnrollment;
    }

    /**
     * Récupère l'ID du programme le plus récent pour un étudiant
     * @param studentId ID de l'étudiant
     * @return l'ID du programme
     */
    public String getLatestProgramIdForStudent(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .max((e1, e2) -> e1.getEnrolledAt().compareTo(e2.getEnrolledAt()))
                .map(Enrollment::getProgramLevelId)
                .orElse(null);
    }

    /**
     * Convertit une entité Enrollment en DTO EnrollmentResponse
     * @param enrollment entité Enrollment
     * @return DTO EnrollmentResponse
     */
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getProgramLevelId(),
                enrollment.getAcademicYear(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}