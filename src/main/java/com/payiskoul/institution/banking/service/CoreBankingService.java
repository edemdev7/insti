package com.payiskoul.institution.banking.service;


import com.payiskoul.institution.banking.dto.AccountCreationRequest;
import com.payiskoul.institution.banking.dto.AccountCreationResponse;

public interface CoreBankingService {
    AccountCreationResponse createAccount(AccountCreationRequest request);
}