package com.payiskoul.institution.banking.service;

import com.payiskoul.institution.banking.dto.AccountCreationRequest;
import com.payiskoul.institution.banking.dto.AccountCreationResponse;
import com.payiskoul.institution.exception.ServiceTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CoreBankingServiceImpl implements CoreBankingService {

    private final RestTemplate restTemplate;
    private final String coreBankingUrl;

    public CoreBankingServiceImpl(
            RestTemplate restTemplate,
            @Value("${payiskoul.core.banking.domain:localhost}") String coreBankingDomain) {
        this.restTemplate = restTemplate;
        log.info("core banking domain: {}",coreBankingDomain);
        this.coreBankingUrl = "http://" + coreBankingDomain + "/v1/accounts";
    }

    @Override
    @Retryable(
            value = {RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 300000) // 5 minutes en millisecondes
    )
    public AccountCreationResponse createAccount(AccountCreationRequest request) {
        try {
            ResponseEntity<AccountCreationResponse> response = restTemplate.postForEntity(
                    coreBankingUrl,
                    request,
                    AccountCreationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RestClientException("Unexpected status code: " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            // Capture et re-lance les exceptions de timeout ou de connexion
            throw e;
        } catch (Exception e) {
            // Autre type d'exception
            throw new RestClientException("Error calling core banking service: " + e.getMessage(), e);
        }
    }

    @Recover
    public AccountCreationResponse recoverCreateAccount(RestClientException e, AccountCreationRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("institutionId", request.id());
        details.put("institutionName", request.fullName());

        throw new ServiceTimeoutException(
                "Core banking service is unavailable after multiple attempts",
                details
        );
    }
}