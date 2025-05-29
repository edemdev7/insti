package com.payiskoul.institution.program.controller;

import com.payiskoul.institution.program.dto.PaginatedPrograms;
import com.payiskoul.institution.program.dto.ProgramLevelCreateRequest;
import com.payiskoul.institution.program.dto.ProgramLevelResponse;
import com.payiskoul.institution.program.service.ProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@Tag(name = "Programmes", description = "API pour la gestion des niveaux de programme")
public class ProgramController {

    private final ProgramService programService;

    // Valeurs par défaut pour la pagination
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    @PostMapping("/v1/institutions/{id}/programs")
    @Operation(summary = "Créer un niveau pour une institution",
            description = "Crée un nouveau niveau de programme pour une institution donnée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Niveau créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProgramLevelResponse.class))),
            @ApiResponse(responseCode = "404", description = "Institution introuvable"),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides")
    })
    public ResponseEntity<ProgramLevelResponse> createProgramLevel(
            @Parameter(description = "ID de l'institution", required = true)
            @PathVariable("id") String institutionId,

            @Valid @RequestBody ProgramLevelCreateRequest request
    ) {
        log.info("Requête de création d'un niveau pour l'institution {}: {}", institutionId, request.name());

        ProgramLevelResponse response = programService.createProgramLevel(institutionId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/v1/institutions/{institutionId}/programs")
    @Operation(summary = "Obtenir les niveaux d'une institution",
            description = "Récupère la liste paginée des niveaux d'une institution, filtrée par année académique si spécifiée")
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

        PaginatedPrograms response = programService.getProgramLevels(institutionId, year, page, size);

        return ResponseEntity.ok(response);
    }
}