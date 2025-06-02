package com.payiskoul.institution.student.service;

import com.payiskoul.institution.exception.BusinessException;
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
import com.payiskoul.institution.tuition.model.TuitionStatus;
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

/**
 * Service spécialisé pour les inscriptions aux offres professionnelles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TrainingOfferRepository trainingOfferRepository;
    private final TuitionService tuitionService;

    /**
     * Inscrit un étudiant à une offre professionnelle
     * Pour les offres professionnelles, pas de gestion de classes
     */
    @Transactional
    public EnrollmentResponse enrollToProfessionalOffer(String studentId, String offerId) {
        log.info("Inscription de l'étudiant {} à l'offre professionnelle {}", studentId, offerId);

        // Vérifier que l'étudiant existe
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                        Map.of("studentId", studentId)));

        // Vérifier que l'offre existe et est professionnelle
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (offer.getOfferType() != OfferType.PROFESSIONAL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Cette offre n'est pas une offre professionnelle",
                    Map.of("offerType", offer.getOfferType()));
        }

        // Vérifier que l'offre est publiée et approuvée
        if (!offer.getIsPublished() || !offer.getIsApproved()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Cette offre n'est pas disponible pour inscription",
                    Map.of("offerId", offerId, "published", offer.getIsPublished(),
                            "approved", offer.getIsApproved()));
        }

        // Vérifier si l'étudiant n'est pas déjà inscrit
        if (enrollmentRepository.existsByStudentIdAndProgramLevelIdAndAcademicYear(
                studentId, offerId, offer.getAcademicYear())) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS,
                    "L'étudiant est déjà inscrit à cette offre",
                    Map.of("studentId", studentId, "offerId", offerId));
        }

        // Créer l'inscription (sans classe pour les offres professionnelles)
        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .programLevelId(offerId)
                .institutionId(offer.getInstitutionId())
                .classroomId(null) // Pas de classe pour les offres professionnelles
                .academicYear(offer.getAcademicYear())
                .status(Enrollment.EnrollmentStatus.ENROLLED)
                .enrolledAt(LocalDateTime.now())
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscription créée avec succès: {}", savedEnrollment.getId());

        // Créer le statut de paiement si nécessaire
        if (offer.getTuitionAmount() != null && offer.getTuitionAmount().compareTo(BigDecimal.ZERO) > 0) {
            tuitionService.createTuitionStatus(
                    savedEnrollment.getId(),
                    studentId,
                    student.getMatricule(),
                    offer.getTuitionAmount(),
                    offer.getCurrency(),
                    BigDecimal.ZERO
            );
        }

        return mapToEnrollmentResponse(savedEnrollment, student, offer);
    }

    /**
     * Récupère les étudiants inscrits à une offre professionnelle
     */
    public StudentListResponse getStudentsByProfessionalOffer(String institutionId, String offerId,
                                                              StudentQueryParams queryParams) {
        log.info("Récupération des étudiants pour l'offre professionnelle {} de l'institution {}",
                offerId, institutionId);

        // Vérifier que l'offre existe et appartient à l'institution
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        if (offer.getOfferType() != OfferType.PROFESSIONAL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Cette offre n'est pas une offre professionnelle",
                    Map.of("offerType", offer.getOfferType()));
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

    /**
     * Obtient les statistiques d'inscription pour une offre professionnelle
     */
    public ProfessionalOfferStats getProfessionalOfferStats(String institutionId, String offerId) {
        log.info("Récupération des statistiques pour l'offre professionnelle {} de l'institution {}",
                offerId, institutionId);

        // Vérifier que l'offre existe et appartient à l'institution
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        if (!offer.getInstitutionId().equals(institutionId) || offer.getOfferType() != OfferType.PROFESSIONAL) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Offre invalide", Map.of("institutionId", institutionId, "offerId", offerId));
        }

        // Calculer les statistiques
        long totalEnrollments = enrollmentRepository.countByProgramLevelId(offerId);

        // Compter par statut
        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(""); // TODO: Créer méthode spécifique

        long activeEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .count();

        long completedEnrollments = allEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count();

        // Calculer les revenus (via le service de frais de scolarité)
        BigDecimal totalRevenue = calculateTotalRevenue(offerId);

        return new ProfessionalOfferStats(
                offerId,
                offer.getLabel(),
                totalEnrollments,
                activeEnrollments,
                completedEnrollments,
                totalRevenue,
                offer.getTuitionAmount() != null ? offer.getTuitionAmount() : BigDecimal.ZERO
        );
    }

    /**
     * Marque une inscription comme terminée
     */
    @Transactional
    public EnrollmentStatusUpdateResponse completeEnrollment(String enrollmentId) {
        log.info("Marquage de l'inscription {} comme terminée", enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS,
                        "Inscription introuvable", Map.of("enrollmentId", enrollmentId)));

        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setUpdatedAt(LocalDateTime.now());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Inscription marquée comme terminée avec succès");

        return new EnrollmentStatusUpdateResponse(
                updatedEnrollment.getId(),
                updatedEnrollment.getStatus(),
                updatedEnrollment.getUpdatedAt()
        );
    }

    // ============ MÉTHODES PRIVÉES ============

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment, Student student, TrainingOffer offer) {
        return new EnrollmentResponse(
                enrollment.getId(),
                new StudentInfo(student.getId(), student.getFullName(), null),
                new OfferInfo(offer.getId(), offer.getLabel()),
                enrollment.getInstitutionId(),
                null, // Pas de classe pour les offres professionnelles
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }

    private StudentOfferInfo mapToStudentOfferInfo(Enrollment enrollment, TrainingOffer offer) {
        // Récupérer l'étudiant
        Student student = studentRepository.findById(enrollment.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Étudiant introuvable",
                        Map.of("studentId", enrollment.getStudentId())));

        // Pour les offres professionnelles, pas de classe
        //ClassroomInfo classroomInfo = null;

        // Récupérer les informations de paiement
        var tuitionStatus = tuitionService.getTuitionStatusByEnrollment(enrollment.getId());

        return new StudentOfferInfo(
                student.getId(),
                student.getMatricule(),
                student.getFullName(),
                student.getEmail(),
                null, // null pour les offres professionnelles
                tuitionStatus.map(TuitionStatus::getPaymentStatus).orElse(null),
                tuitionStatus.map(TuitionStatus::getPaidAmount).orElse(BigDecimal.ZERO),
                tuitionStatus.map(TuitionStatus::getRemainingAmount)
                        .orElse(offer.getTuitionAmount() != null ? offer.getTuitionAmount() : BigDecimal.ZERO)
        );
    }

    private BigDecimal calculateTotalRevenue(String offerId) {
        // Cette méthode devrait calculer le total des revenus pour cette offre
        // En utilisant le service de frais de scolarité
        return BigDecimal.ZERO; // Placeholder
    }

    // ============ CLASSES INTERNES ============

    /**
     * Statistiques pour une offre professionnelle
     */
    public record ProfessionalOfferStats(
            String offerId,
            String offerTitle,
            long totalEnrollments,
            long activeEnrollments,
            long completedEnrollments,
            BigDecimal totalRevenue,
            BigDecimal pricePerStudent
    ) {}
}