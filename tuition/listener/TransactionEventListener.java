package com.payiskoul.institution.tuition.listener;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.tuition.dto.*;
import com.payiskoul.institution.tuition.service.PaymentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Composant responsable de l'écoute des événements de transaction
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

    private final PaymentEventService paymentEventService;
    private final StudentRepository studentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final InstitutionRepository institutionRepository;

    private static final String TUITION_EXCHANGE = "payiskoul.tuition.exchange";
    private static final String TUITION_PAYMENT_CONFIRMED_ROUTING_KEY = "tuition.payment.confirmed";
    private static final String TUITION_PAYMENT_FAILED_ROUTING_KEY = "tuition.payment.failed";

    /**
     * Écoute les événements de transaction créés
     */
    @RabbitListener(queues = "payiskoul.transaction.queue")
    @Retryable(value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Réception d'un événement TransactionCreated: {}", event.getEventId());
        log.info("Category TransactionCreated: {}", event.getPayload().getCategory());

        // Vérifier si c'est un paiement de scolarité
        if (!"TUITION_PAYMENT".equals(event.getPayload().getCategory())) {
            log.debug("Événement ignoré, ce n'est pas un paiement de scolarité");
            return;
        }

        try {
            // Extraire les informations nécessaires
            String matricule = event.getPayload().getPhoneNumber(); // le matricule est stocké dans phoneNumber
            String enrollmentId = extractEnrollmentId(event);
            String institutionId = extractInstitutionId(event);

            String accountId = "";

            // Vérifier que l'étudiant existe
            if (!studentRepository.existsByMatricule(matricule)) {
                publishFailedEvent(event, "Étudiant introuvable: " + matricule);
                return;
            }
            var institution = institutionRepository.findById(institutionId);
            if(institution.isEmpty()) {
                publishFailedEvent(event, "Institution introuvable: " + institutionId);
            }else if(institution.get().getStatus().equals(InstitutionStatus.INACTIVE)
                    || institution.get().getAccountId() == null || institution.get().getAccountId().isBlank()){
                publishFailedEvent(event, "Institution désactivé ou n'a pas de compte: " + institutionId);
                return;
            }else {
                accountId = institution.get().getAccountId();
            }

            PaymentNotificationDTO notification = PaymentNotificationDTO.builder()
                    .enrollmentId(enrollmentId)
                    .institutionAccountId(accountId)
                    .matricule(matricule)
                    .amount(event.getPayload().getAmountReceived())
                    .currency(event.getPayload().getCurrencyCode())
                    .reference(event.getPayload().getReference())
                    .build();


            // Traiter la notification de paiement
            TuitionPaymentEvent tuitionPayment = paymentEventService.processPaymentNotification(notification);

            // Publier l'événement de confirmation
            event.getPayload().setReceiverAccountId(UUID.fromString(accountId));
            publishConfirmedEvent(event, tuitionPayment);

            log.info("Paiement de scolarité traité avec succès pour l'étudiant: {}", matricule);

        } catch (BusinessException be) {
            log.error("Erreur métier lors du traitement du paiement: {}", be.getMessage());
            publishFailedEvent(event, be.getMessage());
        } catch (Exception e) {
            log.error("Erreur technique lors du traitement du paiement: {}", e.getMessage(), e);
            publishFailedEvent(event, "Erreur technique: " + e.getMessage());
        }
    }

    /**
     * Extrait l'ID d'inscription à partir de la description ou des données additionnelles
     */
    private String extractEnrollmentId(TransactionCreatedEvent event) {
        String description = event.getPayload().getDescription();
        log.info("Event description: {}", description);

        if (description != null) {
            Pattern pattern = Pattern.compile("enrollmentId=([^,\\s]+)");
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(1); // extrait la valeur entre enrollmentId= et la virgule
            }
        }

        throw new BusinessException(ErrorCode.INVALID_PAYMENT_DATA,
                "ID d'inscription manquant dans les données de paiement");
    }
//    private String extractEnrollmentId(TransactionCreatedEvent event) {
//        // Dans un cas réel, l'ID d'inscription serait extrait de la description
//        // ou des données additionnelles de la transaction
//        String description = event.getPayload().getDescription();
//        // Format supposé: "Paiement de scolarité pour XXX, enrollmentId=YYY"
//        log.info("Event description: {}",description);
//        if (description != null && description.contains("enrollmentId=")) {
//            return description.substring(description.indexOf("enrollmentId=") + 13);
//        }
//
//        // Si non présent dans la description, vérifier dans les métadonnées
//        // ...
//
//        // Si non trouvé, lever une exception
//        throw new BusinessException(ErrorCode.INVALID_PAYMENT_DATA,
//                "ID d'inscription manquant dans les données de paiement");
//    }

    /**
     * Extrait l'ID d'institution à partir des données additionnelles
     */
    private String extractInstitutionId(TransactionCreatedEvent event) {
        String description = event.getPayload().getDescription();
        log.info("Event description: {}", description);

        if (description != null) {
            Pattern pattern = Pattern.compile("institutionId=([^,\\s]+)");
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        throw new BusinessException(ErrorCode.INVALID_PAYMENT_DATA,
                "ID d'institution manquant dans les données de paiement");
    }

//    private String extractInstitutionId(TransactionCreatedEvent event) {
//        // Même approche que pour extractEnrollmentId
//        // ...
//        // Pour l'exemple, on retourne une valeur fictive
//        return "institution_default";
//    }

    /**
     * Publie un événement de confirmation de paiement
     */
    private void publishConfirmedEvent(TransactionCreatedEvent sourceEvent, TuitionPaymentEvent tuitionPayment) {
        TuitionPaymentConfirmedEvent event = TuitionPaymentConfirmedEvent.builder()
                .eventType("TuitionPaymentConfirmed")
                .eventId(UUID.randomUUID().toString().replace("-", ""))
                .transactionId(sourceEvent.getPayload().getTransactionId())
                .studentMatricule(sourceEvent.getPayload().getPhoneNumber())
                .amountPaid(sourceEvent.getPayload().getAmountReceived())
                .newStatus(tuitionPayment.paymentStatus().name()) // À déterminer dynamiquement
                .remainingAmount(tuitionPayment.remainingAmount()) // À déterminer dynamiquement
                .institutionId(extractInstitutionId(sourceEvent))
                .accountId(sourceEvent.getPayload().getReceiverAccountId())
                .timestamp(OffsetDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                TUITION_EXCHANGE,
                TUITION_PAYMENT_CONFIRMED_ROUTING_KEY,
                event
        );

        log.info("Événement TuitionPaymentConfirmed publié: {}", event.getEventId());
    }

    /**
     * Publie un événement d'échec de paiement
     */
    private void publishFailedEvent(TransactionCreatedEvent sourceEvent, String reason) {
        TuitionPaymentFailedEvent event = TuitionPaymentFailedEvent.builder()
                .eventType("TuitionPaymentFailed")
                .eventId(UUID.randomUUID().toString().replace("-", ""))
                .transactionId(sourceEvent.getPayload().getTransactionId())
                .studentMatricule(sourceEvent.getPayload().getPhoneNumber())
                .institutionId(extractInstitutionId(sourceEvent))
                .reason(reason)
                .timestamp(OffsetDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                TUITION_EXCHANGE,
                TUITION_PAYMENT_FAILED_ROUTING_KEY,
                event
        );

        log.info("Événement TuitionPaymentFailed publié: {}", event.getEventId());
    }
}