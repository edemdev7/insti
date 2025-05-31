package com.payiskoul.institution.student.service;

import com.payiskoul.institution.classroom.service.ClassroomService;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.EnrollmentAlreadyExistsException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.program.model.OfferType;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.dto.*;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.tuition.model.PaymentStatus;
import com.payiskoul.institution.tuition.model.TuitionStatus;
import com.payiskoul.institution.tuition.repository.TuitionStatusRepository;
import com.payiskoul.institution.tuition.service.TuitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TrainingOfferRepository trainingOfferRepository;
    private final TuitionStatusRepository tuitionStatusRepository;
    private final TuitionService tuitionService;
    private final ClassroomService classroomService;

    /**
     * Crée une inscription pour un étudiant à une offre
     */
    @Transactional
    public EnrollmentResponse createEnrollment(OfferEnrollmentRequest request) {
        log.info("Création d'une inscription pour l'étudiant {} vers l'offre {}",
                request.student().id(), request.offer().id());

        // 1. Créer l'étudiant si il n'existe pas
        Student student = getOrCreateStudent(request.student());

        // 2. Vérifier que l'offre existe
        TrainingOffer offer = trainingOfferRepository.findById(request.offer().id())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", request.offer().id())));

        // Vérifier que l'offre appartient à l'institution
        if (!offer.getInstitutionId().equals(request.institutionId())) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", request.institutionId(), "offerId", request.offer().id()));
        }

        // 3. Vérifier si l'étudiant n'est pas déjà inscrit
        if (enrollmentRepository.existsByStudentIdAndProgramLevelIdAndAcademicYear(
                student.getId(), offer.getId(), offer.getAcademicYear())) {
            throw new EnrollmentAlreadyExistsException(
                    "L'étudiant est déjà inscrit à cette offre pour cette année académique",
                    Map.of(
                            "studentId", student.getId(),
                            "offerId", offer.getId(),
                            "academicYear", offer.getAcademicYear()
                    ));
        }

        // 4. Assigner à une classe si c'est une offre académique
        String assignedClassroomId = null;
        if (offer.getOfferType() == OfferType.ACADEMIC) {
            try {
                assignedClassroomId = classroomService.addStudentToClassroom(
                        offer.getId(), request.classroomId());
                log.info("Étudiant assigné à la classe: {}", assignedClassroomId);
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.NO_CLASSROOM_AVAILABLE) {
                    log.warn("Aucune classe disponible pour cette offre: {}", offer.getId());
                } else if (e.getErrorCode() == ErrorCode.CLASSROOM_FULL) {
                    log.warn("La classe spécifiée est complète: {}", request.classroomId());
                } else {
                    throw e;
                }
            }
        }

        // 5. Créer l'inscription
        Enrollment enrollment = Enrollment.builder()
                .studentId(student.getId())
                .programLevelId(offer.getId())
                .institutionId(request.institutionId())
                .classroomId(assignedClassroomId)
                .academicYear(offer.getAcademicYear())
                .status(Enrollment.EnrollmentStatus.ENROLLED)
                .enrolledAt(LocalDateTime.now())
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscription créée avec succès: {}", savedEnrollment.getId());

        // 6. Créer le statut de paiement
        if (offer.getTuitionAmount() != null && offer.getTuitionAmount().compareTo(BigDecimal.ZERO) > 0) {
            tuitionService.createTuitionStatus(
                    savedEnrollment.getId(),
                    student.getId(),
                    student.getMatricule(),
                    offer.getTuitionAmount(),
                    offer.getCurrency(),
                    BigDecimal.ZERO
            );
        }

        return mapToEnrollmentResponse(savedEnrollment, student, offer);
    }

    /**
     * Met à jour le statut d'une inscription
     */
    @Transactional
    public EnrollmentStatusUpdateResponse updateEnrollmentStatus(String enrollmentId,
                                                                 EnrollmentStatusUpdateRequest request) {
        log.info("Mise à jour du statut de l'inscription {} vers {}", enrollmentId, request.status());

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS,
                        "Inscription introuvable", Map.of("enrollmentId", enrollmentId)));

        // Mettre à jour le statut
        enrollment.setStatus(request.status());
        enrollment.setUpdatedAt(LocalDateTime.now());

        // Si le statut est COMPLETED, définir la date de complétion
        if (request.status() == Enrollment.EnrollmentStatus.COMPLETED) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Statut de l'inscription mis à jour avec succès");

        return new EnrollmentStatusUpdateResponse(
                updatedEnrollment.getId(),
                updatedEnrollment.getStatus(),
                updatedEnrollment.getUpdatedAt()
        );
    }

    /**
     * Récupère les étudiants inscrits à une offre
     */
    public StudentListResponse getStudentsByOffer(String institutionId, String offerId,
                                                  StudentQueryParams queryParams) {
        log.info("Récupération des étudiants pour l'offre {} de l'institution {}", offerId, institutionId);

        // Vérifier que l'offre existe et appartient à l'institution
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        // Paramètres de pagination
        int page = queryParams.page() != null ? queryParams.page() : 0;
        int size = queryParams.size() != null ? queryParams.size() : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());

        // Récupérer les inscriptions paginées
        Page<Enrollment> enrollmentsPage = enrollmentRepository.findByProgramLevelId(offerId, pageable);

        // Mapper vers les DTOs avec informations de paiement
        List<StudentOfferInfo> students = enrollmentsPage.getContent().stream()
                .map(enrollment -> mapToStudentOfferInfo(enrollment, offer))
                .collect(Collectors.toList());

        return new StudentListResponse(
                enrollmentsPage.getNumber(),
                enrollmentsPage.getSize(),
                enrollmentsPage.getTotalElements(),
                enrollmentsPage.getTotalPages(),
                students
        );
    }

    // ============ MÉTHODES PRIVÉES ============

    /**
     * Récupère un étudiant existant ou le crée s'il n'existe pas
     */
    private Student getOrCreateStudent(StudentInfo studentInfo) {
        if (studentInfo.id() != null && !studentInfo.id().isEmpty()) {
            // L'étudiant existe déjà
            return studentRepository.findById(studentInfo.id())
                    .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                            Map.of("studentId", studentInfo.id())));
        } else {
            // Créer un nouvel étudiant (cas où seul le nom est fourni)
            // Dans un cas réel, il faudrait plus d'informations
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "L'ID de l'étudiant est obligatoire pour les inscriptions",
                    Map.of("studentName", studentInfo.fullname()));
        }
    }

    /**
     * Mappe une inscription vers le DTO de réponse
     */
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment, Student student, TrainingOffer offer) {
        return new EnrollmentResponse(
                enrollment.getId(),
                new StudentInfo(student.getId(), student.getFullName()),
                new OfferInfo(offer.getId(), offer.getLabel()),
                enrollment.getInstitutionId(),
                enrollment.getClassroomId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }

    /**
     * Mappe une inscription vers les informations d'étudiant avec données de paiement
     */
    private StudentOfferInfo mapToStudentOfferInfo(Enrollment enrollment, TrainingOffer offer) {
        // Récupérer l'étudiant
        Student student = studentRepository.findById(enrollment.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                        Map.of("studentId", enrollment.getStudentId())));

        // Récupérer les informations de classe si applicable
        ClassroomInfo classroomInfo = null;
        if (enrollment.getClassroomId() != null) {
            classroomInfo = new ClassroomInfo(enrollment.getClassroomId(), "Classe"); // Nom simplifié
        }

        // Récupérer les informations de paiement
        List<TuitionStatus> tuitionStatuses = tuitionStatusRepository.findByEnrollmentId(enrollment.getId());
        PaymentStatus paymentStatus = PaymentStatus.UNPAID;
        BigDecimal amountPaid = BigDecimal.ZERO;
        BigDecimal amountRemaining = offer.getTuitionAmount() != null ? offer.getTuitionAmount() : BigDecimal.ZERO;

        if (!tuitionStatuses.isEmpty()) {
            TuitionStatus tuitionStatus = tuitionStatuses.get(0);
            paymentStatus = tuitionStatus.getPaymentStatus();
            amountPaid = tuitionStatus.getPaidAmount();
            amountRemaining = tuitionStatus.getRemainingAmount();
        }

        return new StudentOfferInfo(
                student.getId(),
                student.getMatricule(),
                student.getFullName(),
                student.getEmail(),
                classroomInfo,
                paymentStatus,
                amountPaid,
                amountRemaining
        );
    }
}