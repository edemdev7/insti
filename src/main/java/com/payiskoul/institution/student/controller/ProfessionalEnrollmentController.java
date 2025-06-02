package com.payiskoul.institution.student.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.student.dto.*;
import com.payiskoul.institution.student.service.ProfessionalEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour les inscriptions aux offres de formation professionnelles
 */
@RestController
@RequestMapping("/v1/professional-enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inscriptions professionnelles", description = "API pour les inscriptions aux formations professionnelles")
public class ProfessionalEnrollmentController {

    private final ProfessionalEnrollmentService professionalEnrollmentService;

    @PostMapping
    @Operation(
            summary = "Inscrire un étudiant à une offre professionnelle",
            description = """
                    Inscrit un étudiant à une offre de formation professionnelle.
                    
                    **Spécificités des offres professionnelles :**
                    - Pas de gestion de classes
                    - Inscription directe
                    - Paiement généralement requis
                    - Certification à la fin
                    
                    **Prérequis :**
                    - L'offre doit être publiée et approuvée
                    - L'étudiant ne doit pas être déjà inscrit
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Inscription créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentResponse.class),
                            examples = @ExampleObject(
                                    name = "Inscription réussie",
                                    value = """
                                            {
                                              "id": "prof-enroll-001",
                                              "student": {
                                                "id": "student-123",
                                                "fullname": "Jean Dupont"
                                              },
                                              "offer": {
                                                "id": "prof-offer-001",
                                                "title": "Formation DevOps"
                                              },
                                              "institutionId": "inst-001",
                                              "classroomId": null,
                                              "enrolledAt": "2024-05-25T09:30:00Z",
                                              "status": "ENROLLED"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou offre non disponible",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Étudiant déjà inscrit à cette offre",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EnrollmentResponse> enrollToProfessionalOffer(
            @Parameter(
                    description = "Données d'inscription",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalEnrollmentRequest.class),
                            examples = @ExampleObject(
                                    name = "Inscription à une formation DevOps",
                                    value = """
                                            {
                                              "studentId": "student-123",
                                              "offerId": "prof-offer-001"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ProfessionalEnrollmentRequest request) {

        log.info("Inscription de l'étudiant {} à l'offre professionnelle {}",
                request.studentId(), request.offerId());

        EnrollmentResponse response = professionalEnrollmentService
                .enrollToProfessionalOffer(request.studentId(), request.offerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{enrollmentId}/complete")
    @Operation(
            summary = "Marquer une inscription comme terminée",
            description = """
                    Marque une inscription à une offre professionnelle comme terminée.
                    
                    **Effet :**
                    - Change le statut à COMPLETED
                    - Enregistre la date de fin
                    - Peut déclencher l'émission de certificat
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscription marquée comme terminée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentStatusUpdateResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscription introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EnrollmentStatusUpdateResponse> completeEnrollment(
            @Parameter(description = "ID de l'inscription", required = true)
            @PathVariable String enrollmentId) {

        log.info("Marquage de l'inscription {} comme terminée", enrollmentId);

        EnrollmentStatusUpdateResponse response = professionalEnrollmentService
                .completeEnrollment(enrollmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/institutions/{institutionId}/offers/{offerId}/students")
    @Operation(
            summary = "Liste des étudiants inscrits à une offre professionnelle",
            description = """
                    Récupère la liste paginée des étudiants inscrits à une offre professionnelle.
                    
                    **Informations retournées :**
                    - Informations personnelles de l'étudiant
                    - Matricule unique
                    - Statut de paiement des frais
                    - Montants payés et restants
                    - Pas de classe (spécifique aux offres professionnelles)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des étudiants récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution ou offre introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<StudentListResponse> getStudentsByProfessionalOffer(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre professionnelle", required = true)
            @PathVariable String offerId,

            @Parameter(description = "Numéro de page")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Taille de la page")
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.info("Récupération des étudiants pour l'offre professionnelle {} de l'institution {}",
                offerId, institutionId);

        StudentQueryParams queryParams = new StudentQueryParams(page, size);
        StudentListResponse response = professionalEnrollmentService
                .getStudentsByProfessionalOffer(institutionId, offerId, queryParams);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/institutions/{institutionId}/offers/{offerId}/stats")
    @Operation(
            summary = "Statistiques d'une offre professionnelle",
            description = """
                    Récupère les statistiques détaillées d'une offre professionnelle.
                    
                    **Métriques incluses :**
                    - Nombre total d'inscriptions
                    - Inscriptions actives vs terminées
                    - Revenus générés
                    - Taux de completion
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistiques récupérées avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalEnrollmentService.ProfessionalOfferStats.class)
                    )
            )
    })
    public ResponseEntity<ProfessionalEnrollmentService.ProfessionalOfferStats> getProfessionalOfferStats(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre professionnelle", required = true)
            @PathVariable String offerId) {

        log.info("Récupération des statistiques pour l'offre professionnelle {} de l'institution {}",
                offerId, institutionId);

        ProfessionalEnrollmentService.ProfessionalOfferStats response = professionalEnrollmentService
                .getProfessionalOfferStats(institutionId, offerId);
        return ResponseEntity.ok(response);
    }
}


