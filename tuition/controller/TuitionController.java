package com.payiskoul.institution.tuition.controller;

import com.payiskoul.institution.tuition.dto.TuitionResponse;
import com.payiskoul.institution.tuition.service.TuitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tuitions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Frais de scolarité", description = "API pour la gestion des frais de scolarité")
public class TuitionController {

    private final TuitionService tuitionService;

    @GetMapping("/{matricule}")
    @Operation(summary = "Récupérer les frais de scolarité", description = "Récupère les informations de paiement des frais de scolarité pour un étudiant par son matricule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informations de paiement récupérées avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TuitionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Aucun étudiant trouvé avec ce matricule")
    })
    public ResponseEntity<TuitionResponse> getTuitionsByMatricule(@PathVariable String matricule) {
        log.info("Récupération des frais de scolarité pour l'étudiant avec le matricule: {}", matricule);
        TuitionResponse response = tuitionService.getTuitionsByMatricule(matricule.toUpperCase());
        return ResponseEntity.ok(response);
    }
}