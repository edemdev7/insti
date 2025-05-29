package com.payiskoul.institution.organization.service;

import com.payiskoul.institution.organization.dto.PaginatedInstitutionResponse;
import com.payiskoul.institution.organization.dto.InstitutionResponse;
import com.payiskoul.institution.organization.dto.UserData;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.payiskoul.institution.utils.date.DateTools.convertDatetimeToString;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionQueryService {

    private final InstitutionRepository institutionRepository;

    /**
     * Recherche des institutions avec pagination et filtres optionnels
     * @param page Numéro de page (commence à 0)
     * @param size Taille de la page
     * @param status Statut de l'institution (optionnel)
     * @param country Pays de l'institution (optionnel)
     * @param acronym Acronyme de l'institution (optionnel)
     * @param name Nom de l'institution (optionnel, recherche partielle)
     * @return Réponse paginée contenant les institutions correspondant aux critères
     */
    //@Cacheable(value = "institutions", key = "{#page, #size, #status, #country, #acronym, #name}")
    public PaginatedInstitutionResponse findInstitutions(
            int page, int size, InstitutionStatus status,
            String country, String acronym, String name) {

        log.info("Recherche d'institutions - page:{}, size:{}, status:{}, country:{}, acronym:{}, name:{}",
                page, size, status, country, acronym, name);

        // Limiter la taille maximale de la page
        if (size > 100) {
            size = 100;
            log.warn("Taille de page limitée à 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Institution> institutionsPage;

        // Appliquer les filtres en fonction des paramètres fournis
        if (status != null && country != null) {
            institutionsPage = institutionRepository.findByStatusAndCountry(status, country, "dummy", pageable);
        } else if (status != null) {
            institutionsPage = institutionRepository.findByStatus(status, "dummy", pageable);
        } else if (country != null) {
            institutionsPage = institutionRepository.findByCountry(country, "dummy", pageable);
        } else if (acronym != null) {
            institutionsPage = institutionRepository.findByAcronym(acronym, "dummy", pageable);
        } else if (name != null) {
            institutionsPage = institutionRepository.findByNameContainingIgnoreCase(name, "dummy", pageable);
        } else {
            institutionsPage = institutionRepository.findAll(pageable);
        }

        // Construire la réponse paginée
        return new PaginatedInstitutionResponse(
                institutionsPage.getNumber(),
                institutionsPage.getSize(),
                institutionsPage.getTotalElements(),
                institutionsPage.getTotalPages(),
                institutionsPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    /**
     * Convertit une Institution en InstitutionResponse DTO
     */
    private InstitutionResponse mapToResponse(Institution institution) {
        var user = new UserData(institution.getUserId(), institution.getAccountId());
        return new InstitutionResponse(
                institution.getId(),
//                institution.getExternalId(),
                institution.getName(),
                institution.getAcronym(),
                institution.getType(),
                institution.getAddress(),
                institution.getContact(),
                institution.getWebsite(),
                institution.getDescription(),
                institution.getStatus(),
                user,
                convertDatetimeToString(institution.getCreatedAt()),
                convertDatetimeToString(institution.getUpdatedAt())
        );
    }
}