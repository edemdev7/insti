package com.payiskoul.institution.program.service;

import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.InstitutionNotFoundException;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import com.payiskoul.institution.program.dto.*;
import com.payiskoul.institution.program.model.OfferType;
import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service spécialisé pour la gestion des offres de formation professionnelles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalOfferService {

    private final TrainingOfferRepository trainingOfferRepository;
    private final InstitutionRepository institutionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MongoTemplate mongoTemplate;

    // ============ CRÉATION D'OFFRES PROFESSIONNELLES ============

    /**
     * Crée une nouvelle offre de formation professionnelle
     */
    @Transactional
    @CacheEvict(value = "professionalOffers", key = "{#institutionId, '*'}")
    public ProfessionalOfferResponse createProfessionalOffer(String institutionId,
                                                             ProfessionalOfferCreateRequest request) {
        log.info("Création d'une nouvelle offre professionnelle pour l'institution {}: {}",
                institutionId, request.title());

        // Vérifier que l'institution existe
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable avec l'ID: " + institutionId,
                        Map.of("institutionId", institutionId)
                ));

        // Année académique au format simple pour les offres professionnelles
        String academicYear = String.valueOf(Year.now().getValue());

        // Vérifier l'unicité du titre
        if (trainingOfferRepository.existsByInstitutionIdAndLabelAndAcademicYear(
                institutionId, request.title(), academicYear)) {
            throw new BusinessException(ErrorCode.PROGRAM_LEVEL_ALREADY_EXISTS,
                    "Une offre avec ce titre existe déjà pour cette année",
                    Map.of(
                            "institutionId", institutionId,
                            "title", request.title(),
                            "academicYear", academicYear
                    ));
        }

        // Générer le code de l'offre
        String offerCode = generateProfessionalOfferCode(request.title(),
                institution.getAcronym(), academicYear);

        // Créer l'offre
        TrainingOffer offer = TrainingOffer.builder()
                .institutionId(institutionId)
                .code(offerCode)
                .label(request.title())
                .offerType(OfferType.PROFESSIONAL)
                .description(request.description())
                .duration(request.durationHours())
                .durationUnit(DurationUnit.HOUR)
                .tuitionAmount(request.price())
                .currency("XOF")
                .certification(request.certification())
                .academicYear(academicYear)
                .coverImage(request.coverImage())
                .promotionalVideo(request.promotionalVideo())
                .language(request.language() != null ? request.language() : "Français")
                .prerequisites(request.prerequisites())
                .learningObjectives(request.learningObjectives())
                .targetAudience(request.targetAudience())
                .assessmentMethods(request.assessmentMethods())
                .includedResources(request.includedResources())
                .isPublished(false) // Par défaut non publié
                .isApproved(false) // Par défaut non approuvé
                .createdAt(LocalDateTime.now())
                .build();

        // Sauvegarder
        TrainingOffer savedOffer = trainingOfferRepository.save(offer);
        log.info("Offre professionnelle créée avec succès: {}", savedOffer.getCode());

        return mapToProfessionalResponse(savedOffer, institution);
    }

    // ============ RÉCUPÉRATION D'OFFRES ============

    /**
     * Récupère les offres professionnelles avec filtres
     */
    @Cacheable(value = "professionalOffers", key = "{#institutionId, #queryParams.hashCode()}")
    public ProfessionalOfferListResponse getProfessionalOffers(String institutionId,
                                                               OfferQueryParams queryParams) {
        log.info("Récupération des offres professionnelles pour l'institution {} avec filtres: {}",
                institutionId, queryParams);

        // Vérifier que l'institution existe
        if (!institutionRepository.existsById(institutionId)) {
            throw new InstitutionNotFoundException(
                    "Institution introuvable avec l'ID: " + institutionId,
                    Map.of("institutionId", institutionId)
            );
        }

        // Paramètres de pagination
        int page = queryParams.page() != null ? queryParams.page() : 0;
        int size = queryParams.size() != null ? queryParams.size() : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Construire la requête avec filtres
        Query query = new Query();
        query.addCriteria(Criteria.where("institutionId").is(institutionId));
        query.addCriteria(Criteria.where("offerType").is(OfferType.PROFESSIONAL));

        // Appliquer les filtres
        applyFilters(query, queryParams);

        // Pagination
        query.with(pageable);

        // Exécuter la requête
        List<TrainingOffer> offers = mongoTemplate.find(query, TrainingOffer.class);
        long totalCount = mongoTemplate.count(query.skip(0).limit(0), TrainingOffer.class);

        // Mapper vers les DTOs
        List<ProfessionalOfferSummary> offerSummaries = offers.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return new ProfessionalOfferListResponse(
                page,
                size,
                totalCount,
                (int) Math.ceil((double) totalCount / size),
                offerSummaries
        );
    }

    /**
     * Récupère les détails d'une offre professionnelle
     */
    @Cacheable(value = "professionalOffer", key = "{#institutionId, #offerId}")
    public ProfessionalOfferResponse getProfessionalOfferDetails(String institutionId, String offerId) {
        log.info("Récupération des détails de l'offre professionnelle {} pour l'institution {}",
                offerId, institutionId);

        // Récupérer l'offre
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifier qu'elle appartient à l'institution et qu'elle est professionnelle
        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        if (offer.getOfferType() != OfferType.PROFESSIONAL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Cette offre n'est pas une offre professionnelle",
                    Map.of("offerType", offer.getOfferType()));
        }

        // Récupérer l'institution
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        return mapToProfessionalResponseWithDetails(offer, institution);
    }

    // ============ MODIFICATION D'OFFRES ============

    /**
     * Met à jour une offre professionnelle
     */
    @Transactional
    @CacheEvict(value = {"professionalOffers", "professionalOffer"}, key = "{#institutionId, '*'}")
    public ProfessionalOfferResponse updateProfessionalOffer(String institutionId, String offerId,
                                                             ProfessionalOfferUpdateRequest request) {
        log.info("Mise à jour de l'offre professionnelle {} pour l'institution {}",
                offerId, institutionId);

        // Récupérer l'offre existante
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifications
        validateOfferForUpdate(offer, institutionId);

        // Récupérer l'institution
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        // Mettre à jour les champs
        updateOfferFields(offer, request, institution);

        // Sauvegarder
        TrainingOffer updatedOffer = trainingOfferRepository.save(offer);
        log.info("Offre professionnelle mise à jour avec succès: {}", updatedOffer.getCode());

        return mapToProfessionalResponse(updatedOffer, institution);
    }

    /**
     * Publie ou dépublie une offre
     */
    @Transactional
    @CacheEvict(value = {"professionalOffers", "professionalOffer"}, key = "{#institutionId, '*'}")
    public ProfessionalOfferResponse publishOffer(String institutionId, String offerId,
                                                  OfferPublishRequest request) {
        log.info("Changement du statut de publication de l'offre {} pour l'institution {}: {}",
                offerId, institutionId, request.isPublished());

        TrainingOffer offer = getValidatedOffer(institutionId, offerId);
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        offer.setIsPublished(request.isPublished());
        offer.setUpdatedAt(LocalDateTime.now());

        TrainingOffer updatedOffer = trainingOfferRepository.save(offer);
        log.info("Statut de publication mis à jour: {}", request.isPublished());

        return mapToProfessionalResponse(updatedOffer, institution);
    }

    /**
     * Approuve ou rejette une offre (admin seulement)
     */
    @Transactional
    @CacheEvict(value = {"professionalOffers", "professionalOffer"}, key = "{#institutionId, '*'}")
    public ProfessionalOfferResponse approveOffer(String institutionId, String offerId,
                                                  OfferApprovalRequest request) {
        log.info("Changement du statut d'approbation de l'offre {} pour l'institution {}: {}",
                offerId, institutionId, request.isApproved());

        TrainingOffer offer = getValidatedOffer(institutionId, offerId);
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        if (request.isApproved()) {
            offer.setIsApproved(true);
            offer.setApprovalDate(LocalDateTime.now());
        } else {
            offer.setIsApproved(false);
            offer.setApprovalDate(null);
            offer.setIsPublished(false); // Dépublier si rejeté
        }
        offer.setUpdatedAt(LocalDateTime.now());

        TrainingOffer updatedOffer = trainingOfferRepository.save(offer);
        log.info("Statut d'approbation mis à jour: {}", request.isApproved());

        return mapToProfessionalResponse(updatedOffer, institution);
    }

    // ============ MÉTHODES PRIVÉES ============

    private void applyFilters(Query query, OfferQueryParams queryParams) {
        if (queryParams.title() != null && !queryParams.title().isEmpty()) {
            query.addCriteria(Criteria.where("label").regex(queryParams.title(), "i"));
        }

        if (queryParams.language() != null && !queryParams.language().isEmpty()) {
            query.addCriteria(Criteria.where("language").is(queryParams.language()));
        }

        if (queryParams.minPrice() != null) {
            query.addCriteria(Criteria.where("tuitionAmount").gte(queryParams.minPrice()));
        }

        if (queryParams.maxPrice() != null) {
            query.addCriteria(Criteria.where("tuitionAmount").lte(queryParams.maxPrice()));
        }

        if (queryParams.isPublished() != null) {
            query.addCriteria(Criteria.where("isPublished").is(queryParams.isPublished()));
        }

        if (queryParams.isApproved() != null) {
            query.addCriteria(Criteria.where("isApproved").is(queryParams.isApproved()));
        }
    }

    private String generateProfessionalOfferCode(String title, String institutionAcronym, String year) {
        String prefix = "PROF";
        String acronym = institutionAcronym != null ? institutionAcronym : "INST";
        return prefix + "-" + acronym + "-" + year;
    }

    private void validateOfferForUpdate(TrainingOffer offer, String institutionId) {
        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offer.getId()));
        }

        if (offer.getOfferType() != OfferType.PROFESSIONAL) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Cette offre n'est pas une offre professionnelle",
                    Map.of("offerType", offer.getOfferType()));
        }
    }

    private TrainingOffer getValidatedOffer(String institutionId, String offerId) {
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        validateOfferForUpdate(offer, institutionId);
        return offer;
    }

    private void updateOfferFields(TrainingOffer offer, ProfessionalOfferUpdateRequest request,
                                   Institution institution) {
        if (request.title() != null && !request.title().isEmpty()) {
            offer.setLabel(request.title());
            // Régénérer le code si le titre change
            String newCode = generateProfessionalOfferCode(request.title(),
                    institution.getAcronym(), offer.getAcademicYear());
            offer.setCode(newCode);
        }

        if (request.subtitle() != null) {
            // Note: Le subtitle n'est pas dans le modèle principal,
            // il pourrait être ajouté ou stocké dans la description
        }

        if (request.description() != null) {
            offer.setDescription(request.description());
        }

        if (request.coverImage() != null) {
            offer.setCoverImage(request.coverImage());
        }

        if (request.promotionalVideo() != null) {
            offer.setPromotionalVideo(request.promotionalVideo());
        }

        if (request.price() != null) {
            offer.setTuitionAmount(request.price());
        }

        if (request.durationHours() != null) {
            offer.setDuration(request.durationHours());
            offer.setDurationUnit(DurationUnit.HOUR);
        }

        if (request.language() != null) {
            offer.setLanguage(request.language());
        }

        if (request.prerequisites() != null) {
            offer.setPrerequisites(request.prerequisites());
        }

        if (request.learningObjectives() != null) {
            offer.setLearningObjectives(request.learningObjectives());
        }

        if (request.targetAudience() != null) {
            offer.setTargetAudience(request.targetAudience());
        }

        if (request.assessmentMethods() != null) {
            offer.setAssessmentMethods(request.assessmentMethods());
        }

        if (request.includedResources() != null) {
            offer.setIncludedResources(request.includedResources());
        }

        if (request.certification() != null) {
            offer.setCertification(request.certification());
        }

        offer.setUpdatedAt(LocalDateTime.now());
    }

    private ProfessionalOfferResponse mapToProfessionalResponse(TrainingOffer offer, Institution institution) {
        return new ProfessionalOfferResponse(
                offer.getId(),
                offer.getLabel(),
                null, // subtitle - à ajouter au modèle si nécessaire
                offer.getDescription(),
                offer.getCoverImage(),
                offer.getPromotionalVideo(),
                offer.getTuitionAmount(),
                offer.getDuration(),
                offer.getLanguage(),
                offer.getPrerequisites(),
                offer.getLearningObjectives(),
                offer.getTargetAudience(),
                offer.getAssessmentMethods(),
                offer.getIncludedResources(),
                offer.getCertification(),
                offer.getIsPublished(),
                offer.getIsApproved(),
                offer.getApprovalDate(),
                new InstitutionInfo(institution.getId(), institution.getName()),
                0, // totalStudents - sera calculé séparément
                offer.getCreatedAt()
        );
    }

    private ProfessionalOfferResponse mapToProfessionalResponseWithDetails(TrainingOffer offer,
                                                                           Institution institution) {
        // Calculer le nombre total d'étudiants
        int totalStudents = (int) enrollmentRepository.countByProgramLevelId(offer.getId());

        return new ProfessionalOfferResponse(
                offer.getId(),
                offer.getLabel(),
                null, // subtitle
                offer.getDescription(),
                offer.getCoverImage(),
                offer.getPromotionalVideo(),
                offer.getTuitionAmount(),
                offer.getDuration(),
                offer.getLanguage(),
                offer.getPrerequisites(),
                offer.getLearningObjectives(),
                offer.getTargetAudience(),
                offer.getAssessmentMethods(),
                offer.getIncludedResources(),
                offer.getCertification(),
                offer.getIsPublished(),
                offer.getIsApproved(),
                offer.getApprovalDate(),
                new InstitutionInfo(institution.getId(), institution.getName()),
                totalStudents,
                offer.getCreatedAt()
        );
    }

    private ProfessionalOfferSummary mapToSummary(TrainingOffer offer) {
        // Calculer le nombre d'étudiants pour le résumé
        int totalStudents = (int) enrollmentRepository.countByProgramLevelId(offer.getId());

        return new ProfessionalOfferSummary(
                offer.getId(),
                offer.getLabel(),
                offer.getCoverImage(),
                offer.getTuitionAmount(),
                offer.getDuration(),
                offer.getLanguage(),
                offer.getIsPublished(),
                offer.getIsApproved(),
                totalStudents,
                offer.getCreatedAt()
        );
    }
}