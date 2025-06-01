package com.payiskoul.institution.program.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.program.dto.*;
import com.payiskoul.institution.program.service.TrainingOfferService;
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

/**
 * Contrôleur unifié pour la gestion des offres de formation
 * Remplace les anciens ProgramController et TrainingOfferController
 */
@RestController
@RequestMapping("/v1/institutions/{institutionId}/offers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Offres de formation", description = "API unifiée pour la gestion des offres de formation et programmes")
public class TrainingOfferController {

    private final TrainingOfferService trainingOfferService;

    // ============ CRÉATION D'OFFRES ============

    @PostMapping
    @Operation(
            summary = "Créer une offre de formation",
            description = """
                    Crée une nouvelle offre de formation pour une institution.
                    
                    **Types d'offres supportés :**
                    - **ACADEMIC** : Formations académiques (Licence, Master, etc.)
                      - Année académique au format : YYYY-YYYY (ex: 2024-2025)
                      - Peuvent avoir des classes associées
                    - **PROFESSIONAL** : Formations professionnelles/certifiantes
                      - Année au format : YYYY (ex: 2024)
                      - Inscription directe sans classes
                    
                    **Code généré automatiquement :** FORMAT-ACRONYME-ANNÉE (ex: LIC1-ESATIC-2025)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Offre créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferResponse.class),
                            examples = @ExampleObject(
                                    name = "Offre académique créée",
                                    value = """
                                            {
                                              "id": "offer-001",
                                              "label": "Licence 1 Informatique",
                                              "offerType": "ACADEMIC",
                                              "code": "LIC1-ESATIC-2025",
                                              "description": "Formation de base pour la licence 1",
                                              "duration": 1,
                                              "durationUnit": "YEAR",
                                              "tuitionAmount": 180000,
                                              "currency": "XOF",
                                              "certification": "Licence",
                                              "academicYear": "2024-2025",
                                              "institution": {
                                                "id": "664f82a9e9d034c2fca9b0e2",
                                                "name": "ESATIC"
                                              },
                                              "totalStudents": 0,
                                              "classrooms": []
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
                    description = "Institution introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Offre déjà existante",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<TrainingOfferResponse> createTrainingOffer(
            @Parameter(
                    description = "Identifiant de l'institution",
                    required = true,
                    example = "664f82a9e9d034c2fca9b0e2"
            )
            @PathVariable String institutionId,

            @Parameter(
                    description = "Données de l'offre à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Offre académique",
                                            value = """
                                                    {
                                                      "label": "Licence 1 Informatique",
                                                      "offerType": "ACADEMIC",
                                                      "description": "Formation de base pour la licence 1",
                                                      "duration": 1,
                                                      "durationUnit": "YEAR",
                                                      "tuitionAmount": 180000,
                                                      "certification": "Licence",
                                                      "academicYear": "2024-2025"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Offre professionnelle",
                                            value = """
                                                    {
                                                      "label": "Formation DevOps",
                                                      "offerType": "PROFESSIONAL",
                                                      "description": "Formation certifiante en DevOps",
                                                      "duration": 3,
                                                      "durationUnit": "MONTH",
                                                      "tuitionAmount": 500000,
                                                      "certification": "Certificat DevOps",
                                                      "academicYear": "2024"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody TrainingOfferCreateRequest request) {

        log.info("Création d'une nouvelle offre pour l'institution {}: {}", institutionId, request.label());

        TrainingOfferResponse response = trainingOfferService.createTrainingOffer(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============ ENDPOINT DE COMPATIBILITÉ POUR LES PROGRAMMES ============

    @PostMapping("/programs")
    @Operation(
            summary = "Créer un programme (DÉPRÉCIÉ - Utiliser /offers)",
            description = """
                    **DÉPRÉCIÉ** : Utilisez POST /offers à la place.
                    
                    Cette méthode est maintenue pour la compatibilité ascendante.
                    Elle crée une offre académique basée sur les paramètres du programme.
                    """,
            deprecated = true
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Programme créé avec succès (en tant qu'offre académique)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "404", description = "Institution introuvable")
    })
    public ResponseEntity<TrainingOfferResponse> createProgramLevel(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,
            @Valid @RequestBody ProgramLevelCreateRequest request
    ) {
        log.info("Requête de création d'un niveau pour l'institution {}: {}", institutionId, request.name());
        log.warn("Utilisation de l'endpoint déprécié /programs - migrer vers /offers");

        TrainingOfferResponse response = trainingOfferService.createProgramLevel(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============ RÉCUPÉRATION D'OFFRES ============

    @GetMapping
    @Operation(
            summary = "Lister les offres de formation",
            description = """
                    Récupère la liste paginée des offres de formation d'une institution avec possibilité de filtrage.
                    
                    **Filtres disponibles :**
                    - **type** : Type d'offre (ACADEMIC, PROFESSIONAL)
                    - **label** : Recherche partielle dans le libellé (insensible à la casse)
                    - **code** : Recherche partielle dans le code (insensible à la casse)
                    - **academicYear** : Année académique exacte
                    
                    **Pagination :**
                    - Taille par défaut : 10 éléments
                    - Tri par date de création (plus récent en premier)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des offres récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferListResponse.class),
                            examples = @ExampleObject(
                                    name = "Liste d'offres",
                                    value = """
                                            {
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 25,
                                              "totalPages": 3,
                                              "offers": [
                                                {
                                                  "id": "offer-001",
                                                  "label": "Licence 1 Informatique",
                                                  "offerType": "ACADEMIC",
                                                  "code": "LIC1-ESATIC-2025",
                                                  "academicYear": "2024-2025",
                                                  "tuitionAmount": 180000,
                                                  "currency": "XOF"
                                                }
                                              ]
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
            )
    })
    public ResponseEntity<TrainingOfferListResponse> getTrainingOffers(
            @Parameter(
                    description = "Identifiant de l'institution",
                    required = true,
                    example = "664f82a9e9d034c2fca9b0e2"
            )
            @PathVariable String institutionId,

            @Parameter(description = "Type d'offre", example = "ACADEMIC")
            @RequestParam(required = false) String type,

            @Parameter(description = "Recherche dans le libellé", example = "licence")
            @RequestParam(required = false) String label,

            @Parameter(description = "Recherche dans le code", example = "L1")
            @RequestParam(required = false) String code,

            @Parameter(description = "Année académique", example = "2024-2025")
            @RequestParam(required = false) String academicYear,

            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.info("Récupération des offres pour l'institution {} avec filtres - type:{}, label:{}, code:{}, academicYear:{}",
                institutionId, type, label, code, academicYear);

        TrainingOfferQueryParams queryParams = new TrainingOfferQueryParams(
                type, label, code, academicYear, page, size
        );

        TrainingOfferListResponse response = trainingOfferService.getTrainingOffers(institutionId, queryParams);
        return ResponseEntity.ok(response);
    }

    // ============ ENDPOINT DE COMPATIBILITÉ POUR LES PROGRAMMES ============

    @GetMapping("/programs")
    @Operation(
            summary = "Obtenir les programmes (DÉPRÉCIÉ - Utiliser /offers)",
            description = """
                    **DÉPRÉCIÉ** : Utilisez GET /offers avec type=ACADEMIC à la place.
                    
                    Récupère la liste paginée des offres académiques d'une institution, 
                    filtrée par année académique si spécifiée, formatée comme des programmes.
                    """,
            deprecated = true
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des programmes récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedPrograms.class))),
            @ApiResponse(responseCode = "404", description = "Institution introuvable")
    })
    public ResponseEntity<PaginatedPrograms> getProgramLevels(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "Année académique (format: YYYY-YYYY)")
            @RequestParam(required = false) String year,

            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(required = false, defaultValue = "0") int page,

            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        log.info("Requête de récupération des niveaux pour l'institution {}, année: {}, page: {}, taille: {}",
                institutionId, year != null ? year : "toutes", page, size);
        log.warn("Utilisation de l'endpoint déprécié /programs - migrer vers /offers?type=ACADEMIC");

        PaginatedPrograms response = trainingOfferService.getProgramLevels(institutionId, year, page, size);
        return ResponseEntity.ok(response);
    }

    // ============ DÉTAILS D'UNE OFFRE ============

    @GetMapping("/{offerId}")
    @Operation(
            summary = "Récupérer les détails d'une offre",
            description = """
                    Récupère les détails complets d'une offre de formation, incluant :
                    - Informations de base de l'offre
                    - Informations sur l'institution
                    - Nombre total d'étudiants inscrits
                    - Liste des classes (pour les offres académiques)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Détails de l'offre récupérés avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferResponse.class),
                            examples = @ExampleObject(
                                    name = "Détails d'une offre",
                                    value = """
                                            {
                                              "id": "offer-001",
                                              "label": "Licence 1 Informatique",
                                              "offerType": "ACADEMIC",
                                              "code": "LIC1-ESATIC-2025",
                                              "description": "Formation de base pour la licence 1",
                                              "duration": 1,
                                              "durationUnit": "YEAR",
                                              "tuitionAmount": 180000,
                                              "currency": "XOF",
                                              "certification": "Licence",
                                              "academicYear": "2024-2025",
                                              "institution": {
                                                "id": "664f82a9e9d034c2fca9b0e2",
                                                "name": "ESATIC"
                                              },
                                              "totalStudents": 10,
                                              "classrooms": [
                                                {
                                                  "id": "664f82a9e9d034c2fca9b0e8",
                                                  "name": "Licence 1 A",
                                                  "currentCount": 10,
                                                  "capacity": 30
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
    public ResponseEntity<TrainingOfferResponse> getTrainingOfferDetails(
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
            @PathVariable String offerId) {

        log.info("Récupération des détails de l'offre {} pour l'institution {}", offerId, institutionId);

        TrainingOfferResponse response = trainingOfferService.getTrainingOfferDetails(institutionId, offerId);
        return ResponseEntity.ok(response);
    }

    // ============ MODIFICATION D'OFFRES ============

    @PutMapping("/{offerId}")
    @Operation(
            summary = "Modifier une offre de formation",
            description = """
                    Met à jour une offre de formation existante.
                    
                    **Champs modifiables :**
                    - Libellé (regenerera automatiquement le code)
                    - Type d'offre
                    - Description
                    - Durée et unité de durée
                    - Montant des frais
                    - Certification
                    - Année académique (avec validation selon le type)
                    
                    **Note :** Les champs non fournis (null) ne seront pas modifiés.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Offre mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferResponse.class)
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
                    description = "Institution ou offre introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<TrainingOfferResponse> updateTrainingOffer(
            @Parameter(
                    description = "Identifiant de l'institution",
                    required = true,
                    example = "664f82a9e9d034c2fca9b0e2"
            )
            @PathVariable String institutionId,

            @Parameter(
                    description = "Identifiant de l'offre à modifier",
                    required = true,
                    example = "offer-001"
            )
            @PathVariable String offerId,

            @Parameter(
                    description = "Données de mise à jour de l'offre",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingOfferUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Mise à jour partielle",
                                    value = """
                                            {
                                              "description": "Formation de base pour la licence 1 - Edition 2025",
                                              "tuitionAmount": 200000
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TrainingOfferUpdateRequest request) {

        log.info("Mise à jour de l'offre {} pour l'institution {}", offerId, institutionId);

        TrainingOfferResponse response = trainingOfferService.updateTrainingOffer(institutionId, offerId, request);
        return ResponseEntity.ok(response);
    }
}