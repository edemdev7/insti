package com.payiskoul.institution.organization.service;

import com.payiskoul.institution.exception.InstitutionNotFoundException;
import com.payiskoul.institution.organization.dto.*;
import com.payiskoul.institution.organization.model.Institution;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.repository.InstitutionRepository;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.payiskoul.institution.utils.date.DateTools.convertDatetimeToString;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final TrainingOfferRepository trainingOfferRepository;
    private final EnrollmentRepository enrollmentRepository;

    public InstitutionResponse createInstitution(InstitutionCreateRequest request) {

        Institution institution = Institution.builder()
                .userId(request.userId())
                .accountId(request.accountId())
                .name(request.name())
                .acronym(request.acronym())
                .type(request.type())
                .address(request.address())
                .contact(request.contact())
                .website(request.website())
                .description(request.description())
                .status(InstitutionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Sauvegarder dans la base de données
        Institution savedInstitution = institutionRepository.save(institution);
        log.info("Institution créée avec succès: {}", savedInstitution.getId());

        // Convertir et retourner la réponse
        return mapToResponse(savedInstitution);
    }

    //@Cacheable(value = "institution", key = "#id")
    public InstitutionResponse getInstitution(String id) {
        log.debug("Recherche de l'institution avec l'ID: {}", id);
        Institution institution = findInstitutionById(id);
        return mapToResponse(institution);
    }

    //@CacheEvict(value = "institution", key = "#id")
    public InstitutionResponse updateInstitution(String id, InstitutionUpdateRequest request) {
        log.debug("Mise à jour de l'institution avec l'ID: {}", id);
        Institution institution = findInstitutionById(id);


        if (request.address() != null) {
            institution.setAddress(request.address());
        }

        if (request.contact() != null) {
            institution.setContact(request.contact());
        }

        if (request.website() != null) {
            institution.setWebsite(request.website());
        }

        if (request.description() != null) {
            institution.setDescription(request.description());
        }

        // Sauvegarder les modifications
        Institution updatedInstitution = institutionRepository.save(institution);
        log.info("Institution mise à jour avec succès: {}", updatedInstitution.getId());

        return mapToResponse(updatedInstitution);
    }

    //@CacheEvict(value = "institution", key = "#id")
    public StatusResponse disableInstitution(String id) {
        log.debug("Désactivation de l'institution avec l'ID: {}", id);
        Institution institution = findInstitutionById(id);

        institution.setStatus(InstitutionStatus.INACTIVE);
        institutionRepository.save(institution);

        log.info("Institution désactivée avec succès: {}", institution.getId());
        return new StatusResponse(
                institution.getId(),
                InstitutionStatus.INACTIVE.name(),
                "Institution désactivée avec succès"
        );
    }

    //@CacheEvict(value = "institution", key = "#id")
    public StatusResponse enableInstitution(String id) {
        log.debug("Activation de l'institution avec l'ID: {}", id);
        Institution institution = findInstitutionById(id);

        institution.setStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);

        log.info("Institution activée avec succès: {}", institution.getId());
        return new StatusResponse(
                institution.getId(),
                InstitutionStatus.ACTIVE.name(),
                "Institution activée avec succès"
        );
    }

    // Méthode utilitaire pour trouver une institution par ID
    private Institution findInstitutionById(String id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new InstitutionNotFoundException("L'institution avec l'ID " + id + " est introuvable"));
    }

    // Méthode pour convertir le modèle en DTO de réponse
    private InstitutionResponse mapToResponse(Institution institution) {
        UserData user = new UserData(institution.getUserId(), institution.getAccountId());
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



    /**
     * Génère un rapport CSV des étudiants (équivalent generate_student_report Django)
     */
    public String generateStudentReport(String institutionId) {
        List<Enrollment> enrollments = enrollmentRepository.findByInstitutionId(institutionId);

        StringBuilder csv = new StringBuilder();
        csv.append("Étudiant,Email,Téléphone,Cours,Date d'inscription,Statut de paiement,Montant payé\n");

        for (Enrollment enrollment : enrollments) {
        }

        return csv.toString();
    }


}