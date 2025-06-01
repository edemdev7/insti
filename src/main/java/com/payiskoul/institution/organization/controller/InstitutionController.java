package com.payiskoul.institution.organization.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.organization.dto.*;
import com.payiskoul.institution.organization.model.InstitutionStatus;
import com.payiskoul.institution.organization.service.InstitutionQueryService;
import com.payiskoul.institution.organization.service.InstitutionService;
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
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/institutions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Institutions", description = "API pour la gestion des institutions éducatives de la plateforme PayiSkoul")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final InstitutionQueryService institutionQueryService;

    @GetMapping
    //@PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    @Operation(
            summary = "Récupérer la liste des institutions",
            description = """
                    Récupère la liste paginée des institutions avec possibilité de filtrage par différents critères.
                    
                    **Filtres disponibles :**
                    - **status** : Filtrer par statut (ACTIVE, INACTIVE, PENDING)
                    - **country** : Filtrer par pays 
                    - **acronyme** : Filtrer par acronyme exact
                    - **name** : Recherche partielle dans le nom (insensible à la casse)
                    
                    **Pagination :**
                    - Taille maximale par page : 100 éléments
                    - Page par défaut : 0
                    - Taille par défaut : 10
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des institutions récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedInstitutionResponse.class),
                            examples = @ExampleObject(
                                    name = "Exemple de réponse paginée",
                                    value = """
                                            {
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 25,
                                              "totalPages": 3,
                                              "institutions": [
                                                {
                                                  "id": "6612f2e81cd4e27e9f473bc1",
                                                  "name": "Université de l'Atlantique",
                                                  "acronym": "UA",
                                                  "type": "UNIVERSITY",
                                                  "address": {
                                                    "country": "Côte d'Ivoire",
                                                    "city": "Abidjan"
                                                  },
                                                  "contact": {
                                                    "email": "contact@ua.edu.ci",
                                                    "phone": "+2250102030405"
                                                  },
                                                  "website": "https://www.ua.edu.ci",
                                                  "description": "Université privée axée sur les sciences appliquées",
                                                  "status": "ACTIVE",
                                                  "user": {
                                                    "userId": "550e8400-e29b-41d4-a716-446655440001",
                                                    "accountId": "550e8400-e29b-41d4-a716-446655440002"
                                                  },
                                                  "createdAt": "2025-04-01",
                                                  "updatedAt": null
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paramètres de requête invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<PaginatedInstitutionResponse> getInstitutions(
            @Parameter(
                    description = "Numéro de page (commence à 0)",
                    example = "0",
                    schema = @Schema(minimum = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Nombre d'éléments par page (maximum 100)",
                    example = "10",
                    schema = @Schema(minimum = "1", maximum = "100")
            )
            @RequestParam(defaultValue = "10") int size,

            @Parameter(
                    description = "Filtrer par statut de l'institution",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) InstitutionStatus status,

            @Parameter(
                    description = "Filtrer par pays de l'institution",
                    example = "Côte d'Ivoire"
            )
            @RequestParam(required = false) String country,

            @Parameter(
                    description = "Filtrer par acronyme exact de l'institution",
                    example = "UA"
            )
            @RequestParam(required = false) String acronym,

            @Parameter(
                    description = "Recherche partielle dans le nom de l'institution (insensible à la casse)",
                    example = "Université"
            )
            @RequestParam(required = false) String name
    ) {
        log.info("Requête de récupération des institutions - page:{}, size:{}, status:{}, country:{}, acronym:{}, name:{}",
                page, size, status, country, acronym, name);

        PaginatedInstitutionResponse response = institutionQueryService.findInstitutions(
                page, size, status, country, acronym, name);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(
            summary = "Créer une nouvelle institution",
            description = """
                    Enregistre une nouvelle institution dans le système PayiSkoul.
                    
                    **Champs obligatoires :**
                    - userId : Identifiant de l'utilisateur propriétaire
                    - accountId : Identifiant du compte associé
                    - name : Nom complet de l'institution
                    - type : Type d'institution (UNIVERSITY, SCHOOL, TRAINING_CENTER)
                    - pin : Code PIN à 5 chiffres
                    
                    **Statut initial :** L'institution est créée avec le statut PENDING
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Institution créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution créée",
                                    value = """
                                            {
                                              "id": "6612f2e81cd4e27e9f473bc2",
                                              "name": "Université de l'Atlantique",
                                              "acronym": "UA",
                                              "type": "UNIVERSITY",
                                              "address": {
                                                "country": "Côte d'Ivoire",
                                                "city": "Abidjan",
                                                "street": "Rue des Palmiers",
                                                "postalCode": "01 BP 123"
                                              },
                                              "contact": {
                                                "email": "contact@ua.edu.ci",
                                                "phone": "+2250102030405",
                                                "fax": "+2250102030406"
                                              },
                                              "website": "https://www.ua.edu.ci",
                                              "description": "Université privée axée sur les sciences appliquées",
                                              "status": "PENDING",
                                              "user": {
                                                "userId": "550e8400-e29b-41d4-a716-446655440001",
                                                "accountId": "550e8400-e29b-41d4-a716-446655440002"
                                              },
                                              "createdAt": "2025-04-05T10:30:00",
                                              "updatedAt": null
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
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Erreur de validation",
                                            value = """
                                                    {
                                                      "timestamp": "2025-04-05T10:30:00Z",
                                                      "errorCode": "VALIDATION_FAILED",
                                                      "message": "Les données fournies ne sont pas valides",
                                                      "path": "uri=/v1/institutions",
                                                      "details": {
                                                        "name": "Le nom est obligatoire",
                                                        "pin": "PIN must be 5 digits"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Institution déjà existante",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution existante",
                                    value = """
                                            {
                                              "timestamp": "2025-04-05T10:30:00Z",
                                              "errorCode": "INSTITUTION_ALREADY_EXISTS",
                                              "message": "Une institution avec cet utilisateur existe déjà",
                                              "path": "uri=/v1/institutions",
                                              "details": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<InstitutionResponse> createInstitution(
            @Parameter(
                    description = "Données de l'institution à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Exemple de création d'institution",
                                    value = """
                                            {
                                              "userId": "550e8400-e29b-41d4-a716-446655440001",
                                              "accountId": "550e8400-e29b-41d4-a716-446655440002",
                                              "name": "Université de l'Atlantique",
                                              "acronym": "UA",
                                              "type": "UNIVERSITY",
                                              "address": {
                                                "country": "Côte d'Ivoire",
                                                "city": "Abidjan",
                                                "street": "Rue des Palmiers",
                                                "postalCode": "01 BP 123"
                                              },
                                              "contact": {
                                                "email": "contact@ua.edu.ci",
                                                "phone": "+2250102030405",
                                                "fax": "+2250102030406"
                                              },
                                              "website": "https://www.ua.edu.ci",
                                              "description": "Université privée axée sur les sciences appliquées",
                                              "pin": "12345"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody InstitutionCreateRequest request
    ) {
        log.info("Requête de création d'une institution reçue: {}", request.name());
        InstitutionResponse response = institutionService.createInstitution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    @Operation(
            summary = "Récupérer une institution par son ID",
            description = """
                    Récupère les détails complets d'une institution en utilisant son identifiant unique.
                    
                    **Informations retournées :**
                    - Informations générales (nom, acronyme, type, etc.)
                    - Adresse complète
                    - Informations de contact
                    - Données utilisateur associées
                    - Dates de création et modification
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Institution trouvée et retournée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionResponse.class),
                            examples = @ExampleObject(
                                    name = "Détails d'une institution",
                                    value = """
                                            {
                                              "id": "6612f2e81cd4e27e9f473bc2",
                                              "name": "Université de l'Atlantique",
                                              "acronym": "UA",
                                              "type": "UNIVERSITY",
                                              "address": {
                                                "country": "Côte d'Ivoire",
                                                "city": "Abidjan",
                                                "street": "Rue des Palmiers",
                                                "postalCode": "01 BP 123"
                                              },
                                              "contact": {
                                                "email": "contact@ua.edu.ci",
                                                "phone": "+2250102030405",
                                                "fax": "+2250102030406"
                                              },
                                              "website": "https://www.ua.edu.ci",
                                              "description": "Université privée axée sur les sciences appliquées",
                                              "status": "ACTIVE",
                                              "user": {
                                                "userId": "550e8400-e29b-41d4-a716-446655440001",
                                                "accountId": "550e8400-e29b-41d4-a716-446655440002"
                                              },
                                              "createdAt": "2025-04-05T10:30:00",
                                              "updatedAt": "2025-04-06T14:20:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution non trouvée",
                                    value = """
                                            {
                                              "timestamp": "2025-04-05T10:30:00Z",
                                              "errorCode": "INSTITUTION_NOT_FOUND",
                                              "message": "L'institution avec l'ID 6612f2e81cd4e27e9f473bc2 est introuvable",
                                              "path": "uri=/v1/institutions/6612f2e81cd4e27e9f473bc2",
                                              "details": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<InstitutionResponse> getInstitution(
            @Parameter(
                    description = "Identifiant unique de l'institution",
                    required = true,
                    example = "6612f2e81cd4e27e9f473bc2"
            )
            @PathVariable String id
    ) {
        log.info("Requête d'obtention de l'institution avec ID: {}", id);
        InstitutionResponse response = institutionService.getInstitution(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    @Operation(
            summary = "Modifier une institution existante",
            description = """
                    Met à jour les informations d'une institution existante.
                    
                    **Champs modifiables :**
                    - address : Adresse complète
                    - contact : Informations de contact
                    - website : Site web officiel
                    - description : Description de l'institution
                    
                    **Note :** Les champs non fournis (null) ne seront pas modifiés.
                    Les champs name, acronym, type et userId ne peuvent pas être modifiés.
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Institution mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution mise à jour",
                                    value = """
                                            {
                                              "id": "6612f2e81cd4e27e9f473bc2",
                                              "name": "Université de l'Atlantique",
                                              "acronym": "UA",
                                              "type": "UNIVERSITY",
                                              "address": {
                                                "country": "Côte d'Ivoire",
                                                "city": "Abidjan",
                                                "street": "Nouvelle adresse",
                                                "postalCode": "01 BP 456"
                                              },
                                              "contact": {
                                                "email": "nouveau@ua.edu.ci",
                                                "phone": "+2250102030407"
                                              },
                                              "website": "https://www.nouveau-ua.edu.ci",
                                              "description": "Description mise à jour",
                                              "status": "ACTIVE",
                                              "user": {
                                                "userId": "550e8400-e29b-41d4-a716-446655440001",
                                                "accountId": "550e8400-e29b-41d4-a716-446655440002"
                                              },
                                              "createdAt": "2025-04-05T10:30:00",
                                              "updatedAt": "2025-04-06T14:20:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données de mise à jour invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<InstitutionResponse> updateInstitution(
            @Parameter(
                    description = "Identifiant unique de l'institution à modifier",
                    required = true,
                    example = "6612f2e81cd4e27e9f473bc2"
            )
            @PathVariable String id,

            @Parameter(
                    description = "Données de mise à jour de l'institution",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InstitutionUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Exemple de mise à jour",
                                    value = """
                                            {
                                              "address": {
                                                "country": "Côte d'Ivoire",
                                                "city": "Abidjan",
                                                "street": "Nouvelle adresse",
                                                "postalCode": "01 BP 456"
                                              },
                                              "contact": {
                                                "email": "nouveau@ua.edu.ci",
                                                "phone": "+2250102030407",
                                                "fax": "+2250102030408"
                                              },
                                              "website": "https://www.nouveau-ua.edu.ci",
                                              "description": "Description mise à jour de l'université"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody InstitutionUpdateRequest request
    ) {
        log.info("Requête de mise à jour de l'institution avec ID: {}", id);
        InstitutionResponse response = institutionService.updateInstitution(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/disable")
    //@PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    @Operation(
            summary = "Désactiver une institution",
            description = """
                    Désactive une institution existante en changeant son statut à INACTIVE.
                    
                    **Effets de la désactivation :**
                    - L'institution ne peut plus accepter de nouvelles inscriptions
                    - Les fonctionnalités de paiement peuvent être limitées
                    - L'accès aux services peut être restreint
                    
                    **Permissions requises :** SUPPORT ou ADMIN
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Institution désactivée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution désactivée",
                                    value = """
                                            {
                                              "id": "6612f2e81cd4e27e9f473bc2",
                                              "status": "INACTIVE",
                                              "message": "Institution désactivée avec succès"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé - Permissions SUPPORT ou ADMIN requises",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<StatusResponse> disableInstitution(
            @Parameter(
                    description = "Identifiant unique de l'institution à désactiver",
                    required = true,
                    example = "6612f2e81cd4e27e9f473bc2"
            )
            @PathVariable String id
    ) {
        log.info("Requête de désactivation de l'institution avec ID: {}", id);
        StatusResponse response = institutionService.disableInstitution(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/enable")
    //@PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
    @Operation(
            summary = "Activer une institution",
            description = """
                    Active une institution existante en changeant son statut à ACTIVE.
                    
                    **Effets de l'activation :**
                    - L'institution peut accepter de nouvelles inscriptions
                    - Toutes les fonctionnalités de paiement sont disponibles
                    - Accès complet aux services de la plateforme
                    
                    **Permissions requises :** SUPPORT ou ADMIN
                    """,
            tags = {"Institutions"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Institution activée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatusResponse.class),
                            examples = @ExampleObject(
                                    name = "Institution activée",
                                    value = """
                                            {
                                              "id": "6612f2e81cd4e27e9f473bc2",
                                              "status": "ACTIVE",
                                              "message": "Institution activée avec succès"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Institution introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé - Permissions SUPPORT ou ADMIN requises",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<StatusResponse> enableInstitution(
            @Parameter(
                    description = "Identifiant unique de l'institution à activer",
                    required = true,
                    example = "6612f2e81cd4e27e9f473bc2"
            )
            @PathVariable String id
    ) {
        log.info("Requête d'activation de l'institution avec ID: {}", id);
        StatusResponse response = institutionService.enableInstitution(id);
        return ResponseEntity.ok(response);
    }
}