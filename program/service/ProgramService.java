package com.payiskoul.institution.program.service;

import com.payiskoul.institution.exception.InstitutionNotFoundException;
import com.payiskoul.institution.exception.ProgramLevelAlreadyExistsException;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.program.dto.PaginatedPrograms;
import com.payiskoul.institution.program.dto.ProgramLevelCreateRequest;
import com.payiskoul.institution.program.dto.ProgramLevelResponse;
import com.payiskoul.institution.program.model.ProgramLevel;
import com.payiskoul.institution.program.model.Tuition;
import com.payiskoul.institution.program.repository.ProgramLevelRepository;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramService {

    private final ProgramLevelRepository programLevelRepository;
    private final InstitutionRepository institutionRepository;

    /**
     * Crée un nouveau niveau de programme pour une institution donnée
     * @param institutionId ID de l'institution
     * @param request Données du niveau à créer
     * @return Le niveau créé
     * @throws InstitutionNotFoundException si l'institution n'existe pas
     * @throws ProgramLevelAlreadyExistsException si un programme similaire existe déjà
     */
    //@CacheEvict(value = "programLevels", key = "{#institutionId, '*'}")
    public ProgramLevelResponse createProgramLevel(String institutionId, ProgramLevelCreateRequest request) {
        log.info("Création d'un nouveau niveau pour l'institution {}: {}", institutionId, request.name());

        // Vérifier que l'institution existe
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException(
                        "Institution introuvable avec l'ID: " + institutionId)
                        .withDetail("institutionId", institutionId)
                );

        // Valider le format de l'année académique
        validateAcademicYear(request.academicYear());

        if (programLevelRepository.existsByInstitutionIdAndNameAndAcademicYear(
                institutionId, request.name(), request.academicYear())) {
            throw new ProgramLevelAlreadyExistsException(
                    "Un niveau de programme avec le même nom et la même année académique existe déjà")
                    .withDetail("institutionId", institutionId)
                    .withDetail("programName", request.name())
                    .withDetail("academicYear", request.academicYear());
        }

        // Générer le code du niveau
        String levelCode = generateLevelCode(request.name(), institution.getAcronym(),
                extractCurrentYear(request.academicYear()));

        // Créer le niveau
        ProgramLevel programLevel = ProgramLevel.builder()
                .institutionId(institutionId)
                .code(levelCode)
                .name(request.name())
                .academicYear(request.academicYear())
                .tuition(Tuition.builder()
                        .amount(request.tuition().amount())
                        .currency(request.tuition().currency())
                        .build())
                .duration(request.duration())
                .durationUnit(request.durationUnit())
                .certification(request.certification())
                .createdAt(LocalDateTime.now())
                .build();

        // Sauvegarder en base
        ProgramLevel savedLevel = programLevelRepository.save(programLevel);
        log.info("Niveau créé avec succès: {}", savedLevel.getCode());

        // Retourner la réponse
        return mapToResponse(savedLevel);
    }

    /**
     * Récupère la liste paginée des niveaux d'une institution, filtrée par année académique si spécifiée
     * @param institutionId ID de l'institution
     * @param academicYear Année académique (optionnelle)
     * @param page Numéro de page (commence à 0)
     * @param size Nombre d'éléments par page
     * @return Liste paginée des niveaux
     */
   // @Cacheable(value = "programLevels", key = "{#institutionId, #academicYear, #page, #size}")
    public PaginatedPrograms getProgramLevels(String institutionId, String academicYear, int page, int size) {
        log.info("Récupération des niveaux pour l'institution {} et l'année {}, page {} taille {}",
                institutionId, academicYear != null ? academicYear : "toutes les années", page, size);

        // Vérifier que l'institution existe
        if (!institutionRepository.existsById(institutionId)) {
            throw new InstitutionNotFoundException(
                    "Institution introuvable avec l'ID: " + institutionId)
                    .withDetail("institutionId", institutionId);
        }

        // Créer l'objet pageable avec tri par nom
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        // Récupérer les niveaux paginés (avec ou sans filtre d'année)
        Page<ProgramLevel> programPage;
        if (academicYear != null && !academicYear.isEmpty()) {
            programPage = programLevelRepository.findByInstitutionIdAndAcademicYear(
                    institutionId, academicYear, pageable);
        } else {
            programPage = programLevelRepository.findByInstitutionId(institutionId, pageable);
        }

        // Convertir les entités en DTOs
        List<ProgramLevelResponse> programResponses = programPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Construire l'objet de réponse paginé
        return new PaginatedPrograms(
                programPage.getNumber(),
                programPage.getSize(),
                programPage.getTotalElements(),
                programPage.getTotalPages(),
                programResponses
        );
    }

    /**
     * Génère le code du niveau selon le format: [TYPE_NIVEAU]-[INSTITUTION_CODE]-[ANNEE]
     */
    private String generateLevelCode(String levelName, String institutionAcronym, String year) {
        // Extraire un préfixe de 3-4 caractères à partir du nom du niveau
        String levelPrefix = extractLevelPrefix(levelName);
        return levelPrefix + "-" + institutionAcronym + "-" + year;
    }

    /**
     * Extrait un préfixe à partir du nom du niveau
     * Par exemple: "Licence 2" -> "LIC2"
     */
    private String extractLevelPrefix(String levelName) {
        // Diviser le nom en mots
        String[] words = levelName.split("\\s+");

        StringBuilder prefix = new StringBuilder();

        // Traiter le premier mot (ex: "Licence")
        if (words.length > 0 && words[0].length() >= 3) {
            prefix.append(words[0].substring(0, 3).toUpperCase());
        }

        // Ajouter le numéro s'il existe (ex: "2")
        if (words.length > 1 && words[1].matches("\\d+")) {
            prefix.append(words[1]);
        }

        return prefix.toString();
    }

    /**
     * Extrait l'année courante à partir du format "2025-2026"
     */
    private String extractCurrentYear(String academicYear) {
        return academicYear.split("-")[0];
    }

    /**
     * Valide que l'année académique est au format correct et cohérente
     */
    private void validateAcademicYear(String academicYear) {
        String[] years = academicYear.split("-");
        if (years.length != 2) {
            throw new IllegalArgumentException("Format d'année académique invalide");
        }

        int firstYear = Integer.parseInt(years[0]);
        int secondYear = Integer.parseInt(years[1]);

        if (secondYear != firstYear + 1) {
            throw new IllegalArgumentException("L'année académique devrait être au format [année]-[année+1]");
        }

        // Vérifier que l'année est cohérente avec l'année actuelle
        int currentYear = Year.now().getValue();
        if (firstYear < currentYear - 1 || firstYear > currentYear + 1) {
            log.warn("L'année académique {} ne semble pas correspondre à l'année courante {}",
                    academicYear, currentYear);
        }
    }

    /**
     * Convertit un ProgramLevel en ProgramLevelResponse DTO
     */
    private ProgramLevelResponse mapToResponse(ProgramLevel level) {
        return new ProgramLevelResponse(
                level.getId(),
                level.getInstitutionId(),
                level.getCode(),
                level.getName(),
                level.getAcademicYear(),
                level.getTuition().getAmount(),
                level.getTuition().getCurrency(),
                level.getDuration(),
                level.getDurationUnit(),
                level.getCertification()
        );
    }
}