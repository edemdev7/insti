package com.payiskoul.institution.program.repository;

import com.payiskoul.institution.program.model.OfferType;
import com.payiskoul.institution.program.model.TrainingOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingOfferRepository extends MongoRepository<TrainingOffer, String> {

    // ============ BASIC QUERIES ============

    /**
     * Trouve toutes les offres d'une institution
     */
    List<TrainingOffer> findByInstitutionId(String institutionId);
    Page<TrainingOffer> findByInstitutionId(String institutionId, Pageable pageable);

    /**
     * Trouve les offres par institution et type
     */
    Page<TrainingOffer> findByInstitutionIdAndOfferType(String institutionId, OfferType offerType, Pageable pageable);

    /**
     * Trouve les offres par institution et année académique
     */
    Page<TrainingOffer> findByInstitutionIdAndAcademicYear(String institutionId, String academicYear, Pageable pageable);

    /**
     * Vérification d'existence pour éviter les doublons
     */
    boolean existsByInstitutionIdAndLabelAndAcademicYear(String institutionId, String label, String academicYear);

    // ============ SEARCH QUERIES ============

    /**
     * Recherche par libellé (insensible à la casse)
     */
    @Query("{ 'institutionId': ?0, 'label': { $regex: ?1, $options: 'i' } }")
    Page<TrainingOffer> findByInstitutionIdAndLabelContainingIgnoreCase(
            String institutionId, String label, Pageable pageable);

    /**
     * Recherche par code (insensible à la casse)
     */
    @Query("{ 'institutionId': ?0, 'code': { $regex: ?1, $options: 'i' } }")
    Page<TrainingOffer> findByInstitutionIdAndCodeContainingIgnoreCase(
            String institutionId, String code, Pageable pageable);

    // ============ COMPLEX QUERIES ============

    /**
     * Recherche avec filtres multiples
     */
    @Query("{ $and: [ " +
            "{ 'institutionId': ?0 }, " +
            "{ $or: [ { 'offerType': { $exists: false } }, { 'offerType': ?1 } ] }, " +
            "{ $or: [ { 'label': { $exists: false } }, { 'label': { $regex: ?2, $options: 'i' } } ] }, " +
            "{ $or: [ { 'code': { $exists: false } }, { 'code': { $regex: ?3, $options: 'i' } } ] }, " +
            "{ $or: [ { 'academicYear': { $exists: false } }, { 'academicYear': ?4 } ] } " +
            "] }")
    Page<TrainingOffer> findWithFilters(
            String institutionId,
            OfferType offerType,
            String labelPattern,
            String codePattern,
            String academicYear,
            Pageable pageable);

    /**
     * Trouve les offres académiques (qui peuvent avoir des classes)
     */
    List<TrainingOffer> findByInstitutionIdAndOfferType(String institutionId, OfferType offerType);

    /**
     * Statistiques - compte par type pour une institution
     */
    @Query(value = "{ 'institutionId': ?0 }", count = true)
    long countByInstitutionId(String institutionId);

    @Query(value = "{ 'institutionId': ?0, 'offerType': ?1 }", count = true)
    long countByInstitutionIdAndOfferType(String institutionId, OfferType offerType);
}