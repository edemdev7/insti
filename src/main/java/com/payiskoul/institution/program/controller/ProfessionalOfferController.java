package com.payiskoul.institution.program.controller;

import com.payiskoul.institution.exception.ErrorResponse;
import com.payiskoul.institution.program.dto.*;
import com.payiskoul.institution.program.service.ProfessionalOfferService;
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

import java.math.BigDecimal;

/**
 * Contrôleur spécialisé pour la gestion des offres de formation professionnelles
 */
@RestController
@RequestMapping("/v1/institutions/{institutionId}/professional-offers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Offres professionnelles", description = "API pour la gestion des offres de formation professionnelles")
public class ProfessionalOfferController {

    private final ProfessionalOfferService professionalOfferService;

    // ============ CRÉATION D'OFFRES ============

    @PostMapping
    @Operation(
            summary = "Créer une offre de formation professionnelle",
            description = """
                    Crée une nouvelle offre de formation professionnelle pour une institution.
                    
                    **Caractéristiques des offres professionnelles :**
                    - Formation courte (généralement en heures ou jours)
                    - Certification professionnelle
                    - Prix fixe
                    - Inscription directe sans classes
                    - Contenu multimédia (vidéos, images)
                    
                    **Workflow :**
                    1. Création de l'offre (statut non publié)
                    2. Validation et publication par l'institution
                    3. Approbation par un administrateur
                    4. Ouverture aux inscriptions
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Offre professionnelle créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferResponse.class),
                            examples = @ExampleObject(
                                    name = "Offre créée",
                                    value = """
                                            {
                                              "id": "prof-offer-001",
                                              "title": "Formation DevOps Avancée",
                                              "description": "Maîtrisez les outils DevOps modernes",
                                              "price": 500000,
                                              "durationHours": 40,
                                              "language": "Français",
                                              "certification": "Certificat DevOps",
                                              "isPublished": false,
                                              "isApproved": false,
                                              "totalStudents": 0
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
                    responseCode = "409",
                    description = "Offre avec ce titre déjà existante",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    //@PreAuthorize("hasRole('INSTITUTION_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ProfessionalOfferResponse> createProfessionalOffer(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(
                    description = "Données de l'offre professionnelle à créer",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Création d'offre DevOps",
                                    value = """
                                            {
                                              "title": "Formation DevOps Avancée",
                                              "subtitle": "Maîtrisez les outils DevOps modernes",
                                              "description": "Formation complète sur Docker, Kubernetes, CI/CD...",
                                              "price": 500000,
                                              "durationHours": 40,
                                              "language": "Français",
                                              "prerequisites": "Connaissance de base en informatique",
                                              "learningObjectives": "Déployer des applications avec Docker...",
                                              "targetAudience": "Développeurs, administrateurs système",
                                              "certification": "Certificat DevOps"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ProfessionalOfferCreateRequest request) {

        log.info("Création d'une offre professionnelle pour l'institution {}: {}",
                institutionId, request.title());

        ProfessionalOfferResponse response = professionalOfferService
                .createProfessionalOffer(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============ RÉCUPÉRATION D'OFFRES ============

    @GetMapping
    @Operation(
            summary = "Lister les offres professionnelles",
            description = """
                    Récupère la liste paginée des offres de formation professionnelles avec filtres.
                    
                    **Filtres disponibles :**
                    - **title** : Recherche partielle dans le titre
                    - **language** : Langue de la formation
                    - **minPrice/maxPrice** : Fourchette de prix
                    - **isPublished** : Statut de publication
                    - **isApproved** : Statut d'approbation
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des offres récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferListResponse.class)
                    )
            )
    })
    public ResponseEntity<ProfessionalOfferListResponse> getProfessionalOffers(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "Recherche dans le titre")
            @RequestParam(required = false) String title,

            @Parameter(description = "Langue de la formation")
            @RequestParam(required = false) String language,

            @Parameter(description = "Prix minimum")
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(description = "Prix maximum")
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(description = "Statut de publication")
            @RequestParam(required = false) Boolean isPublished,

            @Parameter(description = "Statut d'approbation")
            @RequestParam(required = false) Boolean isApproved,

            @Parameter(description = "Numéro de page")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Taille de la page")
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.info("Récupération des offres professionnelles pour l'institution {}", institutionId);

        OfferQueryParams queryParams = new OfferQueryParams(
                title, language, minPrice, maxPrice, isPublished, isApproved, page, size
        );

        ProfessionalOfferListResponse response = professionalOfferService
                .getProfessionalOffers(institutionId, queryParams);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{offerId}")
    @Operation(
            summary = "Récupérer les détails d'une offre professionnelle",
            description = """
                    Récupère les détails complets d'une offre de formation professionnelle.
                    
                    **Informations incluses :**
                    - Détails complets de l'offre
                    - Statistiques d'inscription
                    - Statuts de publication et d'approbation
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Détails de l'offre récupérés avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offre introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ProfessionalOfferResponse> getProfessionalOfferDetails(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId) {

        log.info("Récupération des détails de l'offre professionnelle {} pour l'institution {}",
                offerId, institutionId);

        ProfessionalOfferResponse response = professionalOfferService
                .getProfessionalOfferDetails(institutionId, offerId);
        return ResponseEntity.ok(response);
    }

    // ============ MODIFICATION D'OFFRES ============

    @PutMapping("/{offerId}")
    @Operation(
            summary = "Modifier une offre professionnelle",
            description = """
                    Met à jour une offre de formation professionnelle existante.
                    
                    **Champs modifiables :**
                    - Titre, description, prix
                    - Durée, langue, prérequis
                    - Objectifs, public cible
                    - Méthodes d'évaluation, ressources
                    - Contenu multimédia
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Offre mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offre introuvable",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    //@PreAuthorize("hasRole('INSTITUTION_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ProfessionalOfferResponse> updateProfessionalOffer(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,

            @Parameter(description = "Données de mise à jour", required = true)
            @Valid @RequestBody ProfessionalOfferUpdateRequest request) {

        log.info("Mise à jour de l'offre professionnelle {} pour l'institution {}",
                offerId, institutionId);

        ProfessionalOfferResponse response = professionalOfferService
                .updateProfessionalOffer(institutionId, offerId, request);
        return ResponseEntity.ok(response);
    }

    // ============ GESTION DES STATUTS ============

    @PatchMapping("/{offerId}/publish")
    @Operation(
            summary = "Publier/dépublier une offre",
            description = """
                    Change le statut de publication d'une offre professionnelle.
                    
                    **Règles :**
                    - Seule l'institution propriétaire peut publier/dépublier
                    - Une offre non approuvée ne peut pas être publiée
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut de publication mis à jour",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferResponse.class)
                    )
            )
    })
    //@PreAuthorize("hasRole('INSTITUTION_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ProfessionalOfferResponse> publishOffer(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,

            @Parameter(
                    description = "Nouveau statut de publication",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Publier l'offre",
                                    value = """
                                            {
                                              "isPublished": true
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody OfferPublishRequest request) {

        log.info("Changement du statut de publication de l'offre {} : {}", offerId, request.isPublished());

        ProfessionalOfferResponse response = professionalOfferService
                .publishOffer(institutionId, offerId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{offerId}/approve")
    @Operation(
            summary = "Approuver/rejeter une offre",
            description = """
                    Change le statut d'approbation d'une offre professionnelle.
                    
                    **Permissions requises :** ADMIN uniquement
                    
                    **Règles :**
                    - Seuls les administrateurs peuvent approuver/rejeter
                    - Une offre rejetée est automatiquement dépubliée
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut d'approbation mis à jour",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProfessionalOfferResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permissions insuffisantes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalOfferResponse> approveOffer(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable String institutionId,

            @Parameter(description = "ID de l'offre", required = true)
            @PathVariable String offerId,

            @Parameter(
                    description = "Décision d'approbation",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Approuver l'offre",
                                            value = """
                                                    {
                                                      "isApproved": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Rejeter l'offre",
                                            value = """
                                                    {
                                                      "isApproved": false,
                                                      "rejectionReason": "Description insuffisante"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody OfferApprovalRequest request) {

        log.info("Changement du statut d'approbation de l'offre {} : {}", offerId, request.isApproved());

        ProfessionalOfferResponse response = professionalOfferService
                .approveOffer(institutionId, offerId, request);
        return ResponseEntity.ok(response);
    }
}