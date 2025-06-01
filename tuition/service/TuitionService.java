package com.payiskoul.institution.tuition.service;

import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.organization.service.InstitutionService;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.tuition.dto.InstitutionInfo;
import com.payiskoul.institution.tuition.dto.ProgramInfo;
import com.payiskoul.institution.tuition.dto.TuitionInfo;
import com.payiskoul.institution.tuition.dto.TuitionResponse;
import com.payiskoul.institution.tuition.model.TuitionStatus;
import com.payiskoul.institution.tuition.model.PaymentStatus;
import com.payiskoul.institution.tuition.repository.TuitionStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service de frais de scolarité mis à jour pour utiliser le modèle unifié TrainingOffer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TuitionService {

    private final TuitionStatusRepository tuitionStatusRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TrainingOfferRepository trainingOfferRepository; // Remplace ProgramLevelRepository
    private final InstitutionService institutionService;

    /**
     * Récupère les informations de paiement pour un étudiant par son matricule
     * @param matricule le matricule de l'étudiant
     * @return les informations de paiement
     */
    @Cacheable(value = "tuitions", key = "#matricule")
    public TuitionResponse getTuitionsByMatricule(String matricule) {
        log.info("Récupération des frais de scolarité pour l'étudiant avec le matricule: {}", matricule);

        // Rechercher l'étudiant
        Student student = studentRepository.findByMatricule(matricule)
                .orElseThrow(() -> new StudentNotFoundException("Aucun étudiant trouvé avec ce matricule",
                        Map.of("matricule", matricule)));

        // Récupérer les statuts de paiement
        List<TuitionStatus> tuitionStatuses = tuitionStatusRepository.findByMatricule(matricule);

        // Récupérer les informations des offres pour chaque statut
        List<TuitionInfo> tuitionInfos = new ArrayList<>();

        for (TuitionStatus status : tuitionStatuses) {
            // Récupérer l'inscription
            Enrollment enrollment = enrollmentRepository.findById(status.getEnrollmentId())
                    .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

            // Récupérer l'offre (remplace la récupération du programme)
            TrainingOffer trainingOffer = trainingOfferRepository.findById(enrollment.getProgramLevelId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable"));

            var institution = institutionService.getInstitution(enrollment.getInstitutionId());

            // Créer les objets ProgramInfo et InstitutionInfo
            InstitutionInfo institutionInfo = new InstitutionInfo(
                    enrollment.getInstitutionId(),
                    institution.name()
            );

            ProgramInfo programInfo = new ProgramInfo(
                    trainingOffer.getId(),
                    trainingOffer.getCode(),
                    trainingOffer.getLabel(), // Utilise label au lieu de name
                    trainingOffer.getAcademicYear(),
                    institutionInfo
            );

            // Créer l'objet TuitionInfo
            TuitionInfo tuitionInfo = new TuitionInfo(
                    programInfo,
                    status.getTotalAmount(),
                    status.getCurrency(),
                    status.getPaidAmount(),
                    status.getRemainingAmount(),
                    status.getPaymentStatus()
            );

            tuitionInfos.add(tuitionInfo);
        }

        // Retourner la réponse
        return new TuitionResponse(
                student.getMatricule(),
                student.getFullName(),
                tuitionInfos.toArray(new TuitionInfo[0])
        );
    }

    /**
     * Crée un statut de paiement pour une inscription
     * @param enrollmentId ID de l'inscription
     * @param studentId ID de l'étudiant
     * @param matricule matricule de l'étudiant
     * @param totalAmount montant total
     * @param currency devise
     * @param paidAmount montant déjà payé
     * @return le statut de paiement créé
     */
    @Transactional
    public TuitionStatus createTuitionStatus(
            String enrollmentId,
            String studentId,
            String matricule,
            BigDecimal totalAmount,
            String currency,
            BigDecimal paidAmount
    ) {
        log.info("Création d'un statut de paiement pour l'inscription: {}", enrollmentId);

        // Calculer le montant restant et déterminer le statut
        BigDecimal remainingAmount = totalAmount.subtract(paidAmount);
        PaymentStatus paymentStatus = determinePaymentStatus(totalAmount, paidAmount);

        // Créer le statut de paiement
        TuitionStatus tuitionStatus = TuitionStatus.builder()
                .enrollmentId(enrollmentId)
                .studentId(studentId)
                .matricule(matricule)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .currency(currency)
                .paymentStatus(paymentStatus)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        // Sauvegarder le statut
        TuitionStatus savedStatus = tuitionStatusRepository.save(tuitionStatus);
        log.info("Statut de paiement créé avec succès. ID: {}", savedStatus.getId());

        return savedStatus;
    }

    /**
     * Met à jour un statut de paiement suite à un paiement
     * @param matricule matricule de l'étudiant
     * @param enrollmentId ID de l'inscription
     * @param amountPaid montant payé
     * @return le statut de paiement mis à jour
     */
    @Transactional
    @CacheEvict(value = "tuitions", key = "#matricule")
    public TuitionStatus updateTuitionStatus(String matricule, String enrollmentId, BigDecimal amountPaid) {
        log.info("Mise à jour du statut de paiement pour l'inscription: {}, montant: {}", enrollmentId, amountPaid);

        // Récupérer le statut de paiement actuel
        List<TuitionStatus> statuses = tuitionStatusRepository.findByEnrollmentId(enrollmentId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("Aucun statut de paiement trouvé pour cette inscription: " + enrollmentId);
        }

        TuitionStatus status = statuses.get(0);

        // Mettre à jour le montant payé
        BigDecimal newPaidAmount = status.getPaidAmount().add(amountPaid);
        BigDecimal newRemainingAmount = status.getTotalAmount().subtract(newPaidAmount);
        PaymentStatus newPaymentStatus = determinePaymentStatus(status.getTotalAmount(), newPaidAmount);

        // Mettre à jour les champs
        status.setPaidAmount(newPaidAmount);
        status.setRemainingAmount(newRemainingAmount);
        status.setPaymentStatus(newPaymentStatus);
        status.setLastUpdatedAt(LocalDateTime.now());

        // Sauvegarder les modifications
        TuitionStatus updatedStatus = tuitionStatusRepository.save(status);
        log.info("Statut de paiement mis à jour avec succès. Nouveau statut: {}", updatedStatus.getPaymentStatus());

        // Envoyer une notification (commenté pour l'instant)
        // notificationService.sendTuitionStatusNotification(
        //         matricule,
        //         updatedStatus.getId(),
        //         newPaymentStatus.name(),
        //         Map.of(
        //                 "totalAmount", status.getTotalAmount(),
        //                 "paidAmount", newPaidAmount,
        //                 "remainingAmount", newRemainingAmount,
        //                 "paymentAmount", amountPaid,
        //                 "currency", status.getCurrency()
        //         )
        // );

        return updatedStatus;
    }

    /**
     * Récupère un statut de paiement par ID d'inscription
     * @param enrollmentId ID de l'inscription
     * @return le statut de paiement
     */
    public Optional<TuitionStatus> getTuitionStatusByEnrollment(String enrollmentId) {
        log.debug("Récupération du statut de paiement pour l'inscription: {}", enrollmentId);

        List<TuitionStatus> statuses = tuitionStatusRepository.findByEnrollmentId(enrollmentId);
        return statuses.isEmpty() ? Optional.empty() : Optional.of(statuses.get(0));
    }

    /**
     * Vérifie si un étudiant a payé ses frais pour une offre
     * @param studentId ID de l'étudiant
     * @param offerId ID de l'offre
     * @return true si payé, false sinon
     */
    public boolean isOfferPaidByStudent(String studentId, String offerId) {
        log.debug("Vérification du paiement pour l'étudiant {} et l'offre {}", studentId, offerId);

        // Rechercher l'inscription de l'étudiant pour cette offre
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getProgramLevelId().equals(offerId)) {
                List<TuitionStatus> statuses = tuitionStatusRepository.findByEnrollmentId(enrollment.getId());
                if (!statuses.isEmpty()) {
                    TuitionStatus status = statuses.get(0);
                    boolean isPaid = status.getPaymentStatus() == PaymentStatus.PAID;
                    log.debug("Statut de paiement pour l'inscription {}: {}", enrollment.getId(), status.getPaymentStatus());
                    return isPaid;
                }
            }
        }

        log.debug("Aucune inscription trouvée pour l'étudiant {} et l'offre {}", studentId, offerId);
        return false;
    }

    /**
     * Calcule le montant total des frais impayés pour un étudiant
     * @param matricule matricule de l'étudiant
     * @return montant total impayé
     */
    public BigDecimal getTotalUnpaidAmount(String matricule) {
        log.debug("Calcul du montant total impayé pour l'étudiant: {}", matricule);

        List<TuitionStatus> statuses = tuitionStatusRepository.findByMatricule(matricule);

        BigDecimal totalUnpaid = statuses.stream()
                .map(TuitionStatus::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Montant total impayé pour {}: {}", matricule, totalUnpaid);
        return totalUnpaid;
    }

    /**
     * Calcule le montant total payé pour un étudiant
     * @param matricule matricule de l'étudiant
     * @return montant total payé
     */
    public BigDecimal getTotalPaidAmount(String matricule) {
        log.debug("Calcul du montant total payé pour l'étudiant: {}", matricule);

        List<TuitionStatus> statuses = tuitionStatusRepository.findByMatricule(matricule);

        BigDecimal totalPaid = statuses.stream()
                .map(TuitionStatus::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Montant total payé pour {}: {}", matricule, totalPaid);
        return totalPaid;
    }

    /**
     * Récupère tous les statuts de paiement pour un étudiant
     * @param matricule matricule de l'étudiant
     * @return liste des statuts de paiement
     */
    public List<TuitionStatus> getAllTuitionStatusByMatricule(String matricule) {
        log.debug("Récupération de tous les statuts de paiement pour: {}", matricule);
        return tuitionStatusRepository.findByMatricule(matricule);
    }

    /**
     * Vérifie si un étudiant a des frais impayés
     * @param matricule matricule de l'étudiant
     * @return true si il y a des frais impayés, false sinon
     */
    public boolean hasUnpaidTuition(String matricule) {
        log.debug("Vérification des frais impayés pour: {}", matricule);

        List<TuitionStatus> statuses = tuitionStatusRepository.findByMatricule(matricule);

        boolean hasUnpaid = statuses.stream()
                .anyMatch(status -> status.getPaymentStatus() != PaymentStatus.PAID);

        log.debug("L'étudiant {} a des frais impayés: {}", matricule, hasUnpaid);
        return hasUnpaid;
    }

    /**
     * Met à jour le statut de paiement directement
     * @param enrollmentId ID de l'inscription
     * @param newStatus nouveau statut
     * @return le statut mis à jour
     */
    @Transactional
    public TuitionStatus updatePaymentStatus(String enrollmentId, PaymentStatus newStatus) {
        log.info("Mise à jour directe du statut de paiement pour l'inscription: {} -> {}", enrollmentId, newStatus);

        List<TuitionStatus> statuses = tuitionStatusRepository.findByEnrollmentId(enrollmentId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("Aucun statut de paiement trouvé pour cette inscription: " + enrollmentId);
        }

        TuitionStatus status = statuses.get(0);
        status.setPaymentStatus(newStatus);
        status.setLastUpdatedAt(LocalDateTime.now());

        // Ajuster les montants selon le nouveau statut
        if (newStatus == PaymentStatus.PAID) {
            status.setPaidAmount(status.getTotalAmount());
            status.setRemainingAmount(BigDecimal.ZERO);
        } else if (newStatus == PaymentStatus.UNPAID) {
            status.setPaidAmount(BigDecimal.ZERO);
            status.setRemainingAmount(status.getTotalAmount());
        }
        // Pour PARTIALLY_PAID, garder les montants actuels
        // Pour OVERPAID, REFUNDED, CANCELLED, PENDING_VALIDATION, garder les montants actuels

        TuitionStatus updatedStatus = tuitionStatusRepository.save(status);
        log.info("Statut de paiement mis à jour avec succès: {}", updatedStatus.getPaymentStatus());

        return updatedStatus;
    }

    /**
     * Génère un rapport de paiement pour une offre
     * @param offerId ID de l'offre
     * @return statistiques de paiement
     */
    public TuitionReportSummary generateTuitionReport(String offerId) {
        log.info("Génération du rapport de paiement pour l'offre: {}", offerId);

        // Récupérer toutes les inscriptions pour cette offre
        long totalEnrollments = enrollmentRepository.countByProgramLevelId(offerId);

        // Récupérer tous les statuts de paiement pour cette offre
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // Cette méthode devrait être modifiée
        // TODO: Ajouter une méthode dans EnrollmentRepository pour récupérer par offre

        // Pour l'instant, retourner des données par défaut
        return new TuitionReportSummary(
                offerId,
                totalEnrollments,
                0L, // paidCount
                0L, // partiallyPaidCount
                0L, // unpaidCount
                BigDecimal.ZERO, // totalAmount
                BigDecimal.ZERO, // totalPaid
                BigDecimal.ZERO  // totalRemaining
        );
    }

    /**
     * Détermine le statut de paiement en fonction des montants
     * @param totalAmount montant total
     * @param paidAmount montant payé
     * @return le statut de paiement
     */
    private PaymentStatus determinePaymentStatus(BigDecimal totalAmount, BigDecimal paidAmount) {
        int comparison = paidAmount.compareTo(totalAmount);

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return PaymentStatus.UNPAID;
        } else if (comparison < 0) {
            return PaymentStatus.PARTIALLY_PAID;
        } else if (comparison == 0) {
            return PaymentStatus.PAID;
        } else {
            return PaymentStatus.OVERPAID;
        }
    }

    // ============ CLASSES INTERNES ============

    /**
     * Résumé du rapport de frais de scolarité
     */
    public record TuitionReportSummary(
            String offerId,
            long totalEnrollments,
            long paidCount,
            long partiallyPaidCount,
            long unpaidCount,
            BigDecimal totalAmount,
            BigDecimal totalPaid,
            BigDecimal totalRemaining
    ) {}
}