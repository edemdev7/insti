package com.payiskoul.institution.tuition.listener;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.tuition.dto.PaymentNotificationDTO;
import com.payiskoul.institution.tuition.dto.TuitionPaymentEvent;
import com.payiskoul.institution.tuition.service.PaymentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final PaymentEventService paymentEventService;

    /**
     * Écoute les notifications de paiement et les traite
     * Utilise un mécanisme de retry en cas d'erreur
     * @param notification la notification de paiement
     */
    @RabbitListener(queues = "payiskoul.tuitions.queue")
    @Retryable(value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handlePaymentNotification(PaymentNotificationDTO notification) {
        log.info("Notification de paiement reçue: matricule={}, montant={}, référence={}",
                notification.matricule(), notification.amount(), notification.reference());

        try {
            TuitionPaymentEvent event = paymentEventService.processPaymentNotification(notification);
            log.info("Notification traitée avec succès: {}, nouveau statut: {}",
                    notification.reference(), event.paymentStatus());
        } catch (BusinessException e) {
            // Si c'est une référence dupliquée, nous l'ignorons (idempotence)
            if (e.getErrorCode().name().equals("DUPLICATE_REFERENCE")) {
                log.warn("Notification dupliquée ignorée: {}", notification.reference());
                return;
            }
            // Sinon, on relance l'exception pour activer le retry
            log.error("Erreur lors du traitement de la notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur inattendue lors du traitement de la notification: {}", e.getMessage(), e);
            throw e;
        }
    }
}