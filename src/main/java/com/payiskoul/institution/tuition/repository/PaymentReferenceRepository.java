package com.payiskoul.institution.tuition.repository;

import com.payiskoul.institution.tuition.model.PaymentReference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentReferenceRepository extends MongoRepository<PaymentReference, String> {
    Optional<PaymentReference> findByReference(String reference);
    boolean existsByReference(String reference);
}