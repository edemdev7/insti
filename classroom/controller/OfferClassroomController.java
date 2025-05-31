package com.payiskoul.institution.classroom.controller;

import com.payiskoul.institution.classroom.dto.ClassroomResponse;
import com.payiskoul.institution.classroom.dto.CreateClassroomRequest;
import com.payiskoul.institution.classroom.dto.UpdateClassroomRequest;
import com.payiskoul.institution.classroom.service.OfferClassroomService;
import com.payiskoul.institution.exception.ErrorResponse;
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
@RequestMapping("/v1/institutions/{institutionId}/offers/{offerId}/classrooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classes des offres", description = "API pour la gestion des classes dans les offres académiques")
public class OfferClassroomController {

    private final OfferClassroomService offerClassroomService;

    @PostMapping
    @Operation(
            summary = "Créer une classe dans une offre",
            description = """
                    Crée une nouvelle classe dans une offre de formation académique.
                    
                    **Restrictions :**
                    - Disponible uniquement pour les offres de type ACADEMIC
                    - Le nom de la classe doit être unique dans l'offre
                    - La capacité doit être supérieure à 0
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Classe créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClassroomResponse.class),
                            examples = @ExampleObject(
                                    name = "Classe créée",
                                    value = """
                                            {
                                              "id": "664f82a9e9d034c2fca9b0e2",
                                              "name": "L1-A",
                                              "capacity": 30,
                                              "currentCount": 0,
                                              "createdAt": "2025-01-23"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides ou offre non académique",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution ou offre introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Classe avec ce nom déjà existante",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ClassroomResponse> createClassroom(
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

            @Parameter(
                    description = "Données de la classe à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateClassroomRequest.class),
                            examples = @ExampleObject(
                                    name = "Création de classe",
                                    value = """
                                            {
                                              "name": "L1-A",
                                              "capacity": 30
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateClassroomRequest request) {

        log.info("Création d'une classe pour l'offre {} de l'institution {}: {}",
                offerId, institutionId, request.name());

        ClassroomResponse response = offerClassroomService.createClassroomForOffer(
                institutionId, offerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{classroomId}")
    @Operation(
            summary = "Modifier une classe",
            description = """
                    Met à jour les informations d'une classe existante.
                    
                    **Champs modifiables :**
                    - Nom de la classe
                    - Capacité d'accueil
                    
                    **Restrictions :**
                    - La nouvelle capacité ne peut pas être inférieure au nombre d'étudiants actuels
                    - Le nom doit rester unique dans l'offre
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Classe mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClassroomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides (ex: capacité trop faible)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution, offre ou classe introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ClassroomResponse> updateClassroom(
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

            @Parameter(
                    description = "Identifiant de la classe à modifier",
                    required = true,
                    example = "class-a"
            )
            @PathVariable String classroomId,

            @Parameter(
                    description = "Données de mise à jour de la classe",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateClassroomRequest.class),
                            examples = @ExampleObject(
                                    name = "Mise à jour de classe",
                                    value = """
                                            {
                                              "name": "L1-A",
                                              "capacity": 35
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UpdateClassroomRequest request) {

        log.info("Mise à jour de la classe {} pour l'offre {} de l'institution {}",
                classroomId, offerId, institutionId);

        ClassroomResponse response = offerClassroomService.updateClassroomForOffer(
                institutionId, offerId, classroomId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{classroomId}")
    @Operation(
            summary = "Supprimer une classe",
            description = """
                    Supprime une classe d'une offre de formation.
                    
                    **Restrictions :**
                    - Interdite si des étudiants sont inscrits dans cette classe
                    - Seules les classes vides peuvent être supprimées
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Classe supprimée avec succès"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Suppression interdite (classe non vide)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution, offre ou classe introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Void> deleteClassroom(
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

            @Parameter(
                    description = "Identifiant de la classe à supprimer",
                    required = true,
                    example = "class-a"
            )
            @PathVariable String classroomId) {

        log.info("Suppression de la classe {} pour l'offre {} de l'institution {}",
                classroomId, offerId, institutionId);

        offerClassroomService.deleteClassroomForOffer(institutionId, offerId, classroomId);
        return ResponseEntity.noContent().build();
    }
}