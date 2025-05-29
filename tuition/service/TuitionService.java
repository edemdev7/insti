package com.payiskoul.institution.tuition.service;

import com.payiskoul.institution.exception.StudentNotFoundException;
import com.payiskoul.institution.organization.service.InstitutionService;
import com.payiskoul.institution.program.model.ProgramLevel;
import com.payiskoul.institution.program.repository.ProgramLevelRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class TuitionService {

    private final TuitionStatusRepository tuitionStatusRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgramLevelRepository programLevelRepository;
//    private final NotificationService notificationService;
    private final InstitutionService institutionService;

    /**
     * Récupère les informations de paiement pour un étudiant par son matricule
     * @param matricule le matricule de l'étudiant
     * @return les informations de paiement
     */
    //@Cacheable(value = "tuitions", key = "#matricule")
    public TuitionResponse getTuitionsByMatricule(String matricule) {
        log.info("Récupération des frais de scolarité pour l'étudiant avec le matricule: {}", matricule);

        // Rechercher l'étudiant
        Student student = studentRepository.findByMatricule(matricule)
                .orElseThrow(() -> new StudentNotFoundException("Aucun étudiant trouvé avec ce matricule",
                        Map.of("matricule", matricule)));

        // Récupérer les statuts de paiement
        List<TuitionStatus> tuitionStatuses = tuitionStatusRepository.findByMatricule(matricule);

        // Récupérer les informations des programmes pour chaque statut
        List<TuitionInfo> tuitionInfos = new ArrayList<>();

        for (TuitionStatus status : tuitionStatuses) {
            // Récupérer l'inscription
            Enrollment enrollment = enrollmentRepository.findById(status.getEnrollmentId())
                    .orElseThrow(() -> new RuntimeException("Inscription introuvable"));

            // Récupérer le programme
            ProgramLevel programLevel = programLevelRepository.findById(enrollment.getProgramLevelId())
                    .orElseThrow(() -> new RuntimeException("Programme introuvable"));
            var institution = institutionService.getInstitution(enrollment.getInstitutionId());
            // Créer les objets ProgramInfo et InstitutionInfo
            InstitutionInfo institutionInfo = new InstitutionInfo(
                    enrollment.getInstitutionId(),
                    institution.name()
            );

            ProgramInfo programInfo = new ProgramInfo(
                    programLevel.getId(),
                    programLevel.getCode(),
                    programLevel.getName(),
                    programLevel.getAcademicYear(),
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

        // Envoyer une notification
//        notificationService.sendTuitionStatusNotification(
//                matricule,
//                savedStatus.getId(),
//                paymentStatus.name(),
//                Map.of(
//                        "totalAmount", totalAmount,
//                        "paidAmount", paidAmount,
//                        "remainingAmount", remainingAmount,
//                        "currency", currency
//                )
//        );

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
    //@CacheEvict(value = "tuitions", key = "#matricule")
    public TuitionStatus updateTuitionStatus(String matricule, String enrollmentId, BigDecimal amountPaid) {
        log.info("Mise à jour du statut de paiement pour l'inscription: {}, montant: {}", enrollmentId, amountPaid);

        // Récupérer le statut de paiement actuel
        List<TuitionStatus> statuses = tuitionStatusRepository.findByEnrollmentId(enrollmentId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("Aucun statut de paiement trouvé pour cette inscription");
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

        // Envoyer une notification
//        notificationService.sendTuitionStatusNotification(
//                matricule,
//                updatedStatus.getId(),
//                newPaymentStatus.name(),
//                Map.of(
//                        "totalAmount", status.getTotalAmount(),
//                        "paidAmount", newPaidAmount,
//                        "remainingAmount", newRemainingAmount,
//                        "paymentAmount", amountPaid,
//                        "currency", status.getCurrency()
//                )
//        );

        return updatedStatus;
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
}