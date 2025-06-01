package com.payiskoul.institution.program.service;

import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.exception.InstitutionNotFoundException;
import com.payiskoul.institution.exception.ProgramLevelAlreadyExistsException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service unifié pour la gestion des offres de formation
 * Remplace l'ancien ProgramService et enrichit TrainingOfferService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingOfferService {

    private final TrainingOfferRepository trainingOfferRepository;
    private final InstitutionRepository institutionRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ============ CRÉATION D'OFFRES ============

    /**
     * Crée une nouvelle offre de formation (unifie la création de programmes et d'offres)
     */
    @Transactional
    @CacheEvict(value = "trainingOffers", key = "{#institutionId, '*'}")
    public TrainingOfferResponse createTrainingOffer(String institutionId, TrainingOfferCreateRequest request) {
        log.info("Création d'une nouvelle offre pour l'institution {}: {}", institutionId, request.label());

        // Vérifier que l'institution existe
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable avec l'ID: " + institutionId,
                        Map.of("institutionId", institutionId)
                ));

        // Valider l'année académique selon le type d'offre
        validateAcademicYear(request.offerType(), request.academicYear());

        // Vérifier l'unicité
        if (trainingOfferRepository.existsByInstitutionIdAndLabelAndAcademicYear(
                institutionId, request.label(), request.academicYear())) {
            throw new ProgramLevelAlreadyExistsException(
                    "Une offre avec le même libellé et la même année académique existe déjà",
                    Map.of(
                            "institutionId", institutionId,
                            "label", request.label(),
                            "academicYear", request.academicYear()
                    ));
        }

        // Générer le code de l'offre
        String offerCode = generateOfferCode(request.label(), institution.getAcronym(),
                extractYear(request.academicYear()));

        // Créer l'offre
        TrainingOffer offer = TrainingOffer.builder()
                .institutionId(institutionId)
                .code(offerCode)
                .label(request.label())
                .offerType(request.offerType())
                .description(request.description())
                .duration(request.duration())
                .durationUnit(request.durationUnit())
                .tuitionAmount(request.tuitionAmount())
                .currency("XOF")
                .certification(request.certification())
                .academicYear(request.academicYear())
                .createdAt(LocalDateTime.now())
                .build();

        // Sauvegarder
        TrainingOffer savedOffer = trainingOfferRepository.save(offer);
        log.info("Offre créée avec succès: {}", savedOffer.getCode());

        return mapToResponse(savedOffer, institution);
    }

    /**
     * Méthode de compatibilité pour créer une offre à partir d'une requête de programme
     */
    @Transactional
    @CacheEvict(value = "trainingOffers", key = "{#institutionId, '*'}")
    public TrainingOfferResponse createProgramLevel(String institutionId, ProgramLevelCreateRequest request) {
        log.info("Création d'un niveau pour l'institution {}: {}", institutionId, request.name());

        // Convertir la requête de programme en requête d'offre
        TrainingOfferCreateRequest offerRequest = new TrainingOfferCreateRequest(
                request.name(),
                OfferType.ACADEMIC, // Les programmes sont toujours académiques
                null, // description
                request.duration(),
                request.durationUnit(),
                request.tuition().amount(),
                request.certification(),
                request.academicYear()
        );

        return createTrainingOffer(institutionId, offerRequest);
    }

    // ============ RÉCUPÉRATION D'OFFRES ============

    /**
     * Récupère les offres d'une institution avec filtres
     */
    @Cacheable(value = "trainingOffers", key = "{#institutionId, #queryParams.hashCode()}")
    public TrainingOfferListResponse getTrainingOffers(String institutionId, TrainingOfferQueryParams queryParams) {
        log.info("Récupération des offres pour l'institution {} avec filtres: {}", institutionId, queryParams);

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

        // Recherche avec filtres
        Page<TrainingOffer> offersPage = searchOffersWithFilters(institutionId, queryParams, pageable);

        // Mapper vers les DTOs
        List<TrainingOfferSummary> offers = offersPage.getContent().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return new TrainingOfferListResponse(
                offersPage.getNumber(),
                offersPage.getSize(),
                offersPage.getTotalElements(),
                offersPage.getTotalPages(),
                offers
        );
    }

    /**
     * Méthode de compatibilité pour récupérer les offres comme des programmes
     */
    @Cacheable(value = "trainingOffers", key = "{#institutionId, #academicYear, #page, #size}")
    public PaginatedPrograms getProgramLevels(String institutionId, String academicYear, int page, int size) {
        log.info("Récupération des niveaux pour l'institution {} et l'année {}, page {} taille {}",
                institutionId, academicYear != null ? academicYear : "toutes les années", page, size);

        // Créer les paramètres de requête
        TrainingOfferQueryParams queryParams = new TrainingOfferQueryParams(
                "ACADEMIC", // Seulement les offres académiques pour les programmes
                null, // label
                null, // code
                academicYear,
                page,
                size
        );

        // Récupérer les offres
        TrainingOfferListResponse offersResponse = getTrainingOffers(institutionId, queryParams);

        // Convertir en réponse de programmes
        List<ProgramLevelResponse> programResponses = offersResponse.offers().stream()
                .map(this::mapToProgramResponse)
                .collect(Collectors.toList());

        return new PaginatedPrograms(
                offersResponse.page(),
                offersResponse.size(),
                offersResponse.totalElements(),
                offersResponse.totalPages(),
                programResponses
        );
    }

    /**
     * Récupère les détails d'une offre
     */
    @Cacheable(value = "trainingOffer", key = "{#institutionId, #offerId}")
    public TrainingOfferResponse getTrainingOfferDetails(String institutionId, String offerId) {
        log.info("Récupération des détails de l'offre {} pour l'institution {}", offerId, institutionId);

        // Récupérer l'offre
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifier qu'elle appartient à l'institution
        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        // Récupérer l'institution
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        return mapToResponseWithDetails(offer, institution);
    }

    // ============ MODIFICATION D'OFFRES ============

    /**
     * Met à jour une offre existante
     */
    @Transactional
    @CacheEvict(value = {"trainingOffers", "trainingOffer"}, key = "{#institutionId, '*'}")
    public TrainingOfferResponse updateTrainingOffer(String institutionId, String offerId,
                                                     TrainingOfferUpdateRequest request) {
        log.info("Mise à jour de l'offre {} pour l'institution {}", offerId, institutionId);

        // Récupérer l'offre existante
        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_LEVEL_NOT_FOUND,
                        "Offre introuvable", Map.of("offerId", offerId)));

        // Vérifier qu'elle appartient à l'institution
        if (!offer.getInstitutionId().equals(institutionId)) {
            throw new BusinessException(ErrorCode.INVALID_INSTITUTION_PROGRAM,
                    "Cette offre n'appartient pas à cette institution",
                    Map.of("institutionId", institutionId, "offerId", offerId));
        }

        // Récupérer l'institution
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable", Map.of("institutionId", institutionId)));

        // Mettre à jour les champs modifiables
        updateOfferFields(offer, request, institution);

        // Sauvegarder
        TrainingOffer updatedOffer = trainingOfferRepository.save(offer);
        log.info("Offre mise à jour avec succès: {}", updatedOffer.getCode());

        return mapToResponse(updatedOffer, institution);
    }

    // ============ MÉTHODES PRIVÉES ============

    /**
     * Recherche les offres avec les filtres appliqués
     */
    private Page<TrainingOffer> searchOffersWithFilters(String institutionId,
                                                        TrainingOfferQueryParams queryParams,
                                                        Pageable pageable) {
        // Si aucun filtre, retourner toutes les offres de l'institution
        if (isEmptyFilters(queryParams)) {
            return trainingOfferRepository.findByInstitutionId(institutionId, pageable);
        }

        // Convertir les paramètres
        OfferType offerType = null;
        if (queryParams.type() != null && !queryParams.type().isEmpty()) {
            try {
                offerType = OfferType.valueOf(queryParams.type().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Type d'offre invalide ignoré: {}", queryParams.type());
            }
        }

        String labelPattern = queryParams.label();
        String codePattern = queryParams.code();
        String academicYear = queryParams.academicYear();

        // Utilise la requête MongoDB personnalisée
        return trainingOfferRepository.findWithFilters(
                institutionId, offerType, labelPattern, codePattern, academicYear, pageable);
    }

    /**
     * Vérifie si tous les filtres sont vides
     */
    private boolean isEmptyFilters(TrainingOfferQueryParams queryParams) {
        return (queryParams.type() == null || queryParams.type().isEmpty()) &&
                (queryParams.label() == null || queryParams.label().isEmpty()) &&
                (queryParams.code() == null || queryParams.code().isEmpty()) &&
                (queryParams.academicYear() == null || queryParams.academicYear().isEmpty());
    }

    /**
     * Met à jour les champs d'une offre existante
     */
    private void updateOfferFields(TrainingOffer offer, TrainingOfferUpdateRequest request, Institution institution) {
        if (request.label() != null && !request.label().isEmpty()) {
            offer.setLabel(request.label());
            // Régénérer le code si le libellé change
            String newCode = generateOfferCode(request.label(), institution.getAcronym(),
                    extractYear(offer.getAcademicYear()));
            offer.setCode(newCode);
        }

        if (request.offerType() != null) {
            offer.setOfferType(request.offerType());
        }

        if (request.description() != null) {
            offer.setDescription(request.description());
        }

        if (request.duration() != null) {
            offer.setDuration(request.duration());
        }

        if (request.durationUnit() != null) {
            offer.setDurationUnit(request.durationUnit());
        }

        if (request.tuitionAmount() != null) {
            offer.setTuitionAmount(request.tuitionAmount());
        }

        if (request.certification() != null) {
            offer.setCertification(request.certification());
        }

        if (request.academicYear() != null && !request.academicYear().isEmpty()) {
            validateAcademicYear(offer.getOfferType(), request.academicYear());
            offer.setAcademicYear(request.academicYear());
        }

        offer.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Génère le code d'une offre
     */
    private String generateOfferCode(String label, String institutionAcronym, String year) {
        String levelPrefix = extractLevelPrefix(label);
        String acronym = institutionAcronym != null ? institutionAcronym : "INST";
        return levelPrefix + "-" + acronym + "-" + year;
    }

    /**
     * Extrait un préfixe du libellé de l'offre
     */
    private String extractLevelPrefix(String label) {
        String[] words = label.split("\\s+");
        StringBuilder prefix = new StringBuilder();

        if (words.length > 0 && words[0].length() >= 3) {
            prefix.append(words[0].substring(0, 3).toUpperCase());
        }

        // Ajouter le numéro s'il existe
        if (words.length > 1 && words[1].matches("\\d+")) {
            prefix.append(words[1]);
        }

        return prefix.toString();
    }

    /**
     * Extrait l'année à partir du format académique
     */
    private String extractYear(String academicYear) {
        if (academicYear.contains("-")) {
            return academicYear.split("-")[0];
        }
        return academicYear;
    }

    /**
     * Valide le format de l'année académique selon le type d'offre
     */
    private void validateAcademicYear(OfferType offerType, String academicYear) {
        if (offerType == OfferType.ACADEMIC) {
            // Format attendu: YYYY-YYYY
            if (!academicYear.matches("^\\d{4}-\\d{4}$")) {
                throw new BusinessException(ErrorCode.INVALID_INPUT,
                        "Pour les offres académiques, l'année doit être au format YYYY-YYYY",
                        Map.of("academicYear", academicYear));
            }

            String[] years = academicYear.split("-");
            int firstYear = Integer.parseInt(years[0]);
            int secondYear = Integer.parseInt(years[1]);

            if (secondYear != firstYear + 1) {
                throw new BusinessException(ErrorCode.INVALID_INPUT,
                        "L'année académique doit être au format [année]-[année+1]",
                        Map.of("academicYear", academicYear));
            }
        } else if (offerType == OfferType.PROFESSIONAL) {
            // Format attendu: YYYY
            if (!academicYear.matches("^\\d{4}$")) {
                throw new BusinessException(ErrorCode.INVALID_INPUT,
                        "Pour les offres professionnelles, l'année doit être au format YYYY",
                        Map.of("academicYear", academicYear));
            }
        }

        // Vérifier que l'année est cohérente
        int currentYear = Year.now().getValue();
        int year = Integer.parseInt(academicYear.split("-")[0]);
        if (year < currentYear - 1 || year > currentYear + 2) {
            log.warn("L'année académique {} semble éloignée de l'année courante {}",
                    academicYear, currentYear);
        }
    }

    // ============ MÉTHODES DE MAPPING ============

    /**
     * Mappe une offre vers le DTO de réponse complet
     */
    private TrainingOfferResponse mapToResponse(TrainingOffer offer, Institution institution) {
        return new TrainingOfferResponse(
                offer.getId(),
                offer.getLabel(),
                offer.getOfferType(),
                offer.getCode(),
                offer.getDescription(),
                offer.getDuration(),
                offer.getDurationUnit(),
                offer.getTuitionAmount(),
                offer.getCurrency(),
                offer.getCertification(),
                offer.getAcademicYear(),
                new InstitutionInfo(institution.getId(), institution.getName()),
                0, // totalStudents sera calculé séparément si nécessaire
                List.of() // classrooms sera ajouté séparément si nécessaire
        );
    }

    /**
     * Mappe une offre vers le DTO de réponse avec détails complets
     */
    private TrainingOfferResponse mapToResponseWithDetails(TrainingOffer offer, Institution institution) {
        // Calculer le nombre total d'étudiants
        int totalStudents = (int) enrollmentRepository.countByProgramLevelId(offer.getId());

        // Récupérer les classes si c'est une offre académique
        List<ClassroomInfo> classrooms = List.of();
        if (offer.getOfferType() == OfferType.ACADEMIC) {
            List<Classroom> classroomList = classroomRepository.findByProgramLevelId(offer.getId());
            classrooms = classroomList.stream()
                    .map(classroom -> new ClassroomInfo(
                            classroom.getId(),
                            classroom.getName(),
                            classroom.getCurrentCount(),
                            classroom.getCapacity()
                    ))
                    .collect(Collectors.toList());
        }

        return new TrainingOfferResponse(
                offer.getId(),
                offer.getLabel(),
                offer.getOfferType(),
                offer.getCode(),
                offer.getDescription(),
                offer.getDuration(),
                offer.getDurationUnit(),
                offer.getTuitionAmount(),
                offer.getCurrency(),
                offer.getCertification(),
                offer.getAcademicYear(),
                new InstitutionInfo(institution.getId(), institution.getName()),
                totalStudents,
                classrooms
        );
    }

    /**
     * Mappe une offre vers le DTO de résumé
     */
    private TrainingOfferSummary mapToSummary(TrainingOffer offer) {
        return new TrainingOfferSummary(
                offer.getId(),
                offer.getLabel(),
                offer.getOfferType(),
                offer.getCode(),
                offer.getAcademicYear(),
                offer.getTuitionAmount(),
                offer.getCurrency()
        );
    }

    /**
     * Mappe une offre vers le DTO de réponse de programme (compatibilité)
     */
    private ProgramLevelResponse mapToProgramResponse(TrainingOfferSummary offer) {
        return new ProgramLevelResponse(
                offer.id(),
                "", // institutionId - sera rempli si nécessaire
                offer.code(),
                offer.label(), // name = label
                offer.academicYear(),
                offer.tuitionAmount(),
                offer.currency(),
                1, // duration par défaut
                DurationUnit.YEAR, // durationUnit par défaut
                "" // certification par défaut
        );
    }
}