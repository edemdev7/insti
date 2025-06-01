package com.payiskoul.institution.identity.service;

import com.payiskoul.institution.exception.UserRegistrationException;
import com.payiskoul.institution.identity.dto.UserRegistrationRequest;
import com.payiskoul.institution.identity.dto.UserRegistrationResponse;
import com.payiskoul.institution.messaging.dto.EmailMessage;
import com.payiskoul.institution.messaging.dto.ErrorNotification;
import com.payiskoul.institution.utils.generator.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${payiskoul.services.identity.url}")
    private String identityServiceUrl;

    @Value("${payiskoul.services.identity.register-endpoint}")
    private String registerEndpoint;

    private static final String EMAIL_EXCHANGE = "payiskoul.email.exchange";
    private static final String EMAIL_ROUTING_KEY = "email.send";

    private static final String NOTIFICATION_EXCHANGE = "payiskoul.notification.exchange";
    private static final String ERROR_NOTIFICATION_ROUTING_KEY = "notification.error";

    /**
     * Crée un utilisateur administrateur pour l'institution de façon asynchrone
     *
     * @param institutionId ID de l'institution
     * @param institutionEmail Email de l'institution
     */
    @Async
    public void createInstitutionAdmin(String institutionId, String institutionEmail) {
        log.info("Création asynchrone d'un utilisateur admin pour l'institution: {}", institutionId);

        try {
            // Générer un mot de passe sécurisé
            String password = PasswordGenerator.generateSecurePassword();

            // Créer la requête d'enregistrement
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email(institutionEmail)
                    .password(password)
                    .role("INSTITUTION_ADMIN")
                    .institutionId(institutionId)
                    .build();

            // Appeler le service d'identité
            UserRegistrationResponse response = registerUserWithRetry(request);

            log.info("Utilisateur admin créé avec succès pour l'institution: {}, userId: {}",
                    institutionId, response.userId());

            // Envoyer un email avec les identifiants
            sendCredentialsEmail(institutionEmail, password);

        } catch (Exception e) {
            log.error("Échec de la création d'utilisateur admin pour l'institution: {}", institutionId, e);
            // L'erreur sera gérée par la méthode de récupération après les tentatives
        }
    }

    /**
     * Appel au service d'identité avec mécanisme de retry
     *
     * @param request Requête d'enregistrement
     * @return Réponse du service d'identité
     */
    @Retryable(
            value = {Exception.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 900000) // 15 minutes (900000 ms)
    )
    public UserRegistrationResponse registerUserWithRetry(UserRegistrationRequest request) {
        log.debug("Tentative d'enregistrement de l'utilisateur: {}", request.email());

        String url = identityServiceUrl + registerEndpoint;
        ResponseEntity<UserRegistrationResponse> response =
                restTemplate.postForEntity(url, request, UserRegistrationResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new UserRegistrationException("Échec de l'enregistrement de l'utilisateur. Code: " +
                    response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Méthode de récupération après l'échec des tentatives de retry
     *
     * @param e Exception capturée
     * @param request Requête d'enregistrement
     */
    @Recover
    public void recoverFromRegistrationFailure(Exception e, UserRegistrationRequest request) {
        log.error("Toutes les tentatives d'enregistrement ont échoué pour l'institution: {}",
                request.institutionId(), e);

        // Envoyer une notification d'erreur
        ErrorNotification notification = ErrorNotification.builder()
                .source("InstitutionService")
                .errorType("USER_REGISTRATION_FAILED")
                .message("L'enregistrement de l'utilisateur admin a échoué après 5 tentatives: " + e.getMessage())
                .institutionId(request.institutionId())
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, ERROR_NOTIFICATION_ROUTING_KEY, notification);

        // Idéalement, enregistrer les données pour une récupération manuelle
        // Ce code serait implémenté selon la stratégie de récupération spécifique
        log.info("Données à traiter manuellement: institutionId={}, email={}",
                request.institutionId(), request.email());
    }

    /**
     * Envoie un email avec les identifiants de connexion
     *
     * @param email Adresse email du destinataire
     * @param password Mot de passe généré
     */
    private void sendCredentialsEmail(String email, String password) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("COMPTE INSTITUTION | UTILISATEUR ADMIN")
                .message(String.format(
                        "Votre compte à bien été créé chez PayiSkoul, vos identifiants sont: \n" +
                                "email: %s \n" +
                                "mot de passe: %s", email, password))
                .build();

        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, EMAIL_ROUTING_KEY, emailMessage);
        log.debug("Email avec identifiants envoyé à: {}", email);
    }
}