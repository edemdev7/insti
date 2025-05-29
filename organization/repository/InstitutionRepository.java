package com.payiskoul.institution.organization.repository;

import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends MongoRepository<Institution, String> {
    Optional<Institution> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
    @Query("{ $and: [ " +
            "{ status: ?0 }, " +
            "{ ?1: { $exists: false } }" +
            " ] }")
    Page<Institution> findByStatus(InstitutionStatus status, String ignoredField, Pageable pageable);

    @Query("{ $and: [ " +
            "{ 'address.country': ?0 }, " +
            "{ ?1: { $exists: false } }" +
            " ] }")
    Page<Institution> findByCountry(String country, String ignoredField, Pageable pageable);

    @Query("{ $and: [ " +
            "{ acronym: ?0 }, " +
            "{ ?1: { $exists: false } }" +
            " ] }")
    Page<Institution> findByAcronym(String acronym, String ignoredField, Pageable pageable);

    @Query("{ $and: [ " +
            "{ name: { $regex: ?0, $options: 'i' } }, " +
            "{ ?1: { $exists: false } }" +
            " ] }")
    Page<Institution> findByNameContainingIgnoreCase(String name, String ignoredField, Pageable pageable);

    @Query("{ $and: [ " +
            "{ status: ?0 }, " +
            "{ 'address.country': ?1 }, " +
            "{ ?2: { $exists: false } }" +
            " ] }")
    Page<Institution> findByStatusAndCountry(InstitutionStatus status, String country, String ignoredField, Pageable pageable);
}
