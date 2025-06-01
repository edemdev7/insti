package com.payiskoul.institution.tuition.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import com.payiskoul.institution.organization.service.InstitutionService;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.tuition.dto.PaymentNotificationDTO;
import com.payiskoul.institution.tuition.dto.TuitionPaymentEvent;
import com.payiskoul.institution.tuition.model.PaymentReference;
import com.payiskoul.institution.tuition.model.TuitionStatus;
import com.payiskoul.institution.tuition.repository.PaymentReferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventService {

    private final TuitionService tuitionService;
    private final PaymentReferenceRepository paymentReferenceRepository;
    private final StudentRepository studentRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String PAYMENT_UPDATED_ROUTING_KEY = "tuition.payment.updated";
    private static final String EXCHANGE_NAME = "payiskoul.events";

    /**
     * Traite une notification de paiement de manière idempotente
     * @param notification la notification de paiement
     * @return les informations sur l'événement de paiement
     */
    @Transactional
    public TuitionPaymentEvent processPaymentNotification(PaymentNotificationDTO notification) {
        log.info("Traitement de la notification de paiement pour le matricule: {}, référence: {}",
                notification.matricule(), notification.reference());

        // Vérifier si la référence a déjà été traitée (idempotence)
        if (paymentReferenceRepository.existsByReference(notification.reference())) {
            log.warn("Référence de paiement déjà traitée: {}", notification.reference());
            throw new BusinessException(ErrorCode.DUPLICATE_REFERENCE,
                    "Cette référence de paiement a déjà été traitée",
                    Map.of("reference", notification.reference()));
        }

        // Mettre à jour le statut de paiement
        TuitionStatus updatedStatus = tuitionService.updateTuitionStatus(
                notification.matricule(),
                notification.enrollmentId(),
                notification.amount()
        );

        // Enregistrer la référence comme traitée
        PaymentReference paymentReference = PaymentReference.builder()
                .reference(notification.reference())
                .matricule(notification.matricule())
                .enrollmentId(notification.enrollmentId())
                .accountId(notification.institutionAccountId())
                .amount(notification.amount())
                .currency(notification.currency())
                .paymentDate(notification.paymentDate())
                .processedAt(LocalDateTime.now())
                .build();

        paymentReferenceRepository.save(paymentReference);
        log.info("Référence de paiement enregistrée: {}", notification.reference());

        // Récupérer les informations de l'étudiant
        Student student = studentRepository.findByMatricule(notification.matricule())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                        "Étudiant introuvable", Map.of("matricule", notification.matricule())));


        // Créer l'événement de paiement
        TuitionPaymentEvent event = new TuitionPaymentEvent(
                UUID.randomUUID().toString(),
                notification.matricule(),
                student.getFullName(),
                notification.enrollmentId(),
                notification.institutionAccountId(),
                notification.amount(),
                updatedStatus.getTotalAmount(),
                updatedStatus.getPaidAmount(),
                updatedStatus.getRemainingAmount(),
                updatedStatus.getCurrency(),
                updatedStatus.getPaymentStatus(),
                notification.reference(),
                notification.paymentDate(),
                LocalDateTime.now()
        );

        // Publier l'événement
        publishPaymentEvent(event);

        return event;
    }

    /**
     * Publie un événement de paiement mis à jour
     * @param event l'événement à publier
     */
    private void publishPaymentEvent(TuitionPaymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, PAYMENT_UPDATED_ROUTING_KEY, event);
            log.info("Événement de paiement publié avec succès: {}", event.reference());
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement de paiement: {}", e.getMessage(), e);
            // L'événement sera tout de même traité, mais nous logguons l'erreur
        }
    }
}
