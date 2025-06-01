package com.payiskoul.institution.student.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.student.dto.EnrollmentResponse;
import com.payiskoul.institution.student.dto.EnrollmentStatusUpdateRequest;
import com.payiskoul.institution.student.dto.EnrollmentStatusUpdateResponse;
import com.payiskoul.institution.student.dto.OfferEnrollmentRequest;
import com.payiskoul.institution.student.dto.StudentListResponse;
import com.payiskoul.institution.student.dto.StudentQueryParams;
import com.payiskoul.institution.student.service.OfferEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inscriptions aux offres", description = "API pour la gestion des inscriptions aux offres de formation")
public class OfferEnrollmentController {

    private final OfferEnrollmentService offerEnrollmentService;

    @PostMapping("/v1/enrollments")
    @Operation(
            summary = "Inscrire un étudiant à une offre",
            description = """
                    Inscrit un étudiant à une offre de formation pour une année académique.
                    
                    **Workflow :**
                    1. Création de l'étudiant dans la collection `students` (si l'étudiant n'existe pas)
                    2. Enregistrement de l'inscription dans la collection `enrollments`
                    3. Assignation automatique à une classe (pour les offres académiques)
                    
                    **Contraintes :**
                    - Tous les champs sont obligatoires sauf `classroomId`
                    - Pour les offres académiques, l'étudiant sera assigné à une classe disponible
                    - Si `classroomId` est fourni, l'étudiant sera assigné à cette classe spécifique
                    """
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
                                              "id": "enroll-001",
                                              "student": {
                                                "id": "1acb56ef5",
                                                "fullname": "Assi Mason"
                                              },
                                              "offer": {
                                                "id": "664f82a9e9d034f2fca9b0e2",
                                                "title": "Master 1"
                                              },
                                              "institutionId": "664f82a9e9d034f2fca9b0e2",
                                              "classroomId": "664f82a9e9d034f2fca9b0e1",
                                              "enrolledAt": "2024-05-25T09:30:00Z",
                                              "status": "ENROLLED"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données d'entrée invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Étudiant ou offre non trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "L'étudiant est déjà inscrit à cette offre",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<EnrollmentResponse> createEnrollment(
            @Parameter(
                    description = "Données d'inscription de l'étudiant",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OfferEnrollmentRequest.class),
                            examples = @ExampleObject(
                                    name = "Inscription avec étudiant existant",
                                    value = """
                                            {
                                              "student": {
                                                "id": "634f82a9e9d034c2fca9b0e2",
                                                "fullname": "Assi Mason"
                                              },
                                              "offer": {
                                                "id": "664f82a9e9d034f2fca9b0e2",
                                                "title": "Master 1"
                                              },
                                              "institutionId": "664f82a9e9d034c2fcb9b0e2",
                                              "classroomId": "664f82a9e9d034c2feca9b0e2"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody OfferEnrollmentRequest request) {

       // log.info("Création d'une inscription pour l'étudiant: {}", request.student().id());
        EnrollmentResponse response = offerEnrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/v1/enrollments/{id}/status")
    @Operation(
            summary = "Modifier le statut d'une inscription",
            description = """
                    Met à jour le statut d'une inscription existante.
                    
                    **Statuts possibles :**
                    - **ENROLLED** : L'apprenant est inscrit activement dans l'offre
                    - **COMPLETED** : L'apprenant a terminé la formation avec succès
                    - **CANCELLED** : L'inscription a été annulée avant le démarrage
                    - **LEFT** : L'apprenant a abandonné la formation en cours de route
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut mis à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentStatusUpdateResponse.class),
                            examples = @ExampleObject(
                                    name = "Statut mis à jour",
                                    value = """
                                            {
                                              "id": "664f82a9e9d034f2fca9b0e1",
                                              "status": "COMPLETED",
                                              "updatedAt": "2024-05-25T16:40:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Statut invalide",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
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
    public ResponseEntity<EnrollmentStatusUpdateResponse> updateEnrollmentStatus(
            @Parameter(
                    description = "Identifiant de l'inscription",
                    required = true,
                    example = "664f82a9e9d034f2fca9b0e1"
            )
            @PathVariable String id,

            @Parameter(
                    description = "Nouveau statut de l'inscription",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentStatusUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Changement de statut",
                                    value = """
                                            {
                                              "status": "COMPLETED"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody EnrollmentStatusUpdateRequest request) {

        log.info("Mise à jour du statut de l'inscription {} vers {}", id, request.status());
        EnrollmentStatusUpdateResponse response = offerEnrollmentService.updateEnrollmentStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/institutions/{institutionId}/offers/{offerId}/students")
    @Operation(
            summary = "Liste des étudiants inscrits à une offre",
            description = """
                    Récupère la liste paginée des étudiants inscrits à une offre de formation.
                    
                    **Informations retournées :**
                    - Informations personnelles de l'étudiant
                    - Matricule unique
                    - Classe assignée (pour les offres académiques)
                    - Statut de paiement des frais
                    - Montants payés et restants
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des étudiants récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentListResponse.class),
                            examples = @ExampleObject(
                                    name = "Liste d'étudiants",
                                    value = """
                                            {
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 25,
                                              "totalPages": 3,
                                              "students": [
                                                {
                                                  "id": "664f82a9e9d034f2fca9b0e2",
                                                  "matricule": "PI-CI-25A0012",
                                                  "fullName": "Ahou Rebecca",
                                                  "email": "rebecca@example.com",
                                                  "classroom": {
                                                    "id": "1203ed41de",
                                                    "name": "L1-A"
                                                  },
                                                  "paymentStatus": "PARTIALLY_PAID",
                                                  "amountPaid": 50000,
                                                  "amountRemaining": 130000
                                                }
                                              ]
                                            }
                                            """
                            )
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
    public ResponseEntity<StudentListResponse> getStudentsByOffer(
            @Parameter(
                    description = "Identifiant de l'institution",
                    required = true,
                    example = "664f82a9e9d034c2fca9b0e2"
            )
            @PathVariable String institutionId,

            @Parameter(
                    description = "Identifiant de l'offre",
                    required = true,
                    example = "offer-001"
            )
            @PathVariable String offerId,

            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.info("Récupération des étudiants pour l'offre {} de l'institution {}",
                offerId, institutionId);

        StudentQueryParams queryParams = new StudentQueryParams(page, size);
        StudentListResponse response = offerEnrollmentService.getStudentsByOffer(
                institutionId, offerId, queryParams);
        return ResponseEntity.ok(response);
    }
}