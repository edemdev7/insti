package com.payiskoul.institution.student.service;

import com.payiskoul.institution.classroom.service.ClassroomService;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.EnrollmentAlreadyExistsException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
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

/**
 * Service d'inscription mis à jour pour utiliser le modèle unifié TrainingOffer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TrainingOfferRepository trainingOfferRepository; // Remplace ProgramLevelRepository
    private final TuitionService tuitionService;
    private final ClassroomService classroomService;

    /**
     * Inscrit un étudiant à une offre de formation
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

        // Vérifier si l'offre existe (remplace la vérification de programme)
        TrainingOffer trainingOffer = trainingOfferRepository.findById(request.programId()) // programId devient offerId
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "L'offre spécifiée n'existe pas",
                        Map.of("offerId", request.programId())));

        // Vérifier si l'étudiant est déjà inscrit à cette offre pour cette année académique
        if (enrollmentRepository.existsByStudentIdAndProgramLevelIdAndAcademicYear(
                request.studentId(), request.programId(), request.academicYear())) {
            throw new EnrollmentAlreadyExistsException(
                    "L'étudiant est déjà inscrit à cette offre pour cette année académique",
                    Map.of(
                            "studentId", request.studentId(),
                            "offerId", request.programId(),
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
                log.warn("Aucune classe disponible pour cette offre: {}", request.programId());
            } else if (e.getErrorCode() == ErrorCode.CLASSROOM_FULL) {
                log.warn("La classe spécifiée est complète: {}", request.classroomId());
            } else {
                throw e;
            }
        }

        // Créer l'inscription
        Enrollment enrollment = enrollStudentToOffer(
                request.studentId(),
                request.programId(),
                trainingOffer.getInstitutionId(),
                request.academicYear(),
                classroomId
        );

        // Retourner la réponse
        return mapToEnrollmentResponse(enrollment);
    }

    /**
     * Inscrit un étudiant à une offre de formation
     * @param studentId ID de l'étudiant
     * @param offerId ID de l'offre (remplace programLevelId)
     * @param institutionId ID de l'institution
     * @param academicYear année académique
     * @param classroomId ID de la classe (optionnel)
     * @return l'inscription créée
     */
    @Transactional
    public Enrollment enrollStudentToOffer(
            String studentId,
            String offerId,
            String institutionId,
            String academicYear,
            String classroomId) {

        // Créer l'inscription
        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .programLevelId(offerId) // Garde le même nom de champ pour compatibilité DB
                .institutionId(institutionId)
                .classroomId(classroomId)  // Peut être null
                .academicYear(academicYear)
                .status(EnrollmentStatus.ENROLLED)
                .enrolledAt(LocalDateTime.now())
                .build();

        // Sauvegarder l'inscription
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscription créée avec succès. ID: {}", savedEnrollment.getId());

        // Récupérer l'offre pour les frais de scolarité
        TrainingOffer trainingOffer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Récupérer l'étudiant pour le matricule
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                        Map.of("studentId", studentId)));

        // Créer un statut de paiement pour cette inscription
        if (trainingOffer.getTuitionAmount() != null) {
            tuitionService.createTuitionStatus(
                    savedEnrollment.getId(),
                    studentId,
                    student.getMatricule(),
                    trainingOffer.getTuitionAmount(),
                    trainingOffer.getCurrency(),
                    BigDecimal.ZERO  // Montant payé initial à 0
            );
        }

        return savedEnrollment;
    }

    /**
     * Méthode de compatibilité - garde l'ancien nom mais utilise la nouvelle logique
     */
    @Transactional
    public Enrollment enrollStudentToProgram(
            String studentId,
            String programLevelId,
            String institutionId,
            String academicYear,
            String classroomId) {

        log.warn("Utilisation de la méthode dépréciée enrollStudentToProgram - migrer vers enrollStudentToOffer");
        return enrollStudentToOffer(studentId, programLevelId, institutionId, academicYear, classroomId);
    }

    /**
     * Récupère l'ID de l'offre la plus récente pour un étudiant
     * @param studentId ID de l'étudiant
     * @return l'ID de l'offre
     */
    public String getLatestOfferIdForStudent(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .max((e1, e2) -> e1.getEnrolledAt().compareTo(e2.getEnrolledAt()))
                .map(Enrollment::getProgramLevelId) // Garde le même nom de champ
                .orElse(null);
    }

    /**
     * Méthode de compatibilité
     */
    public String getLatestProgramIdForStudent(String studentId) {
        log.warn("Utilisation de la méthode dépréciée getLatestProgramIdForStudent - migrer vers getLatestOfferIdForStudent");
        return getLatestOfferIdForStudent(studentId);
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
                enrollment.getProgramLevelId(), // Devient offerId logiquement
                enrollment.getAcademicYear(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}