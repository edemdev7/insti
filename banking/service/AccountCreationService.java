package com.payiskoul.institution.banking.service;

import com.payiskoul.institution.banking.dto.AccountCreationRequest;
import com.payiskoul.institution.banking.dto.AccountCreationResponse;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCreationService {

    private final CoreBankingService coreBankingService;
    private final InstitutionRepository institutionRepository;

    @Async
    public void createBankAccount(Institution institution, String pin) {
        log.info("Starting asynchronous bank account creation for institution: {}", institution.getId());
        try {
            // Création de la requête
            AccountCreationRequest request = AccountCreationRequest.builder()
                    .id(institution.getId())
                    .fullName(institution.getName())
                    .country(institution.getAddress() != null ? institution.getAddress().getCountry() : "CI")
                    .phone(institution.getContact() != null ? institution.getContact().getPhone() : "")
                    .currency("XOF")
                    .customerType(mapInstitutionTypeToCustomerType(institution.getType()))
                    .pin(pin)
                    .build();

            // Appel du service Core Banking
            AccountCreationResponse response = coreBankingService.createAccount(request);

            // Mise à jour de l'institution avec l'ID du compte
            institution.setAccountId(response.accountId());
            institutionRepository.save(institution);

            log.info("Bank account successfully created for institution: {}, accountId: {}",
                    institution.getId(), response.accountId());

        } catch (Exception e) {
            log.error("Failed to create bank account for institution: {}, error: {}",
                    institution.getId(), e.getMessage(), e);

            // Mise à jour du statut de l'institution en cas d'échec
            institution.setStatus(InstitutionStatus.INACTIVE);
            institutionRepository.save(institution);

            log.info("Institution status updated to INACTIVE after failed bank account creation: {}",
                    institution.getId());
        }
    }

    private String mapInstitutionTypeToCustomerType(com.payiskoul.institution.organization.model.InstitutionType type) {
        return "PARENT"; // Pour simplifier, on utilise toujours PARENT comme spécifié dans l'exemple
    }
}
