package com.payiskoul.institution.student.controller;

import com.payiskoul.institution.student.dto.StudentImportResult;
import com.payiskoul.institution.student.service.StudentImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Contrôleur d'importation d'étudiants mis à jour pour utiliser le modèle unifié TrainingOffer
 */
@RestController
@RequestMapping("/v1/students/import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Importation d'étudiants", description = "API pour l'importation d'étudiants depuis des fichiers Excel ou CSV")
public class StudentImportController {

    private final StudentImportService studentImportService;

    // ============ ENDPOINTS PRINCIPAUX (AVEC OFFRES) ============

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des étudiants depuis un fichier CSV",
            description = """
                    Importe des étudiants depuis un fichier CSV et les inscrit à l'offre spécifiée.
                    
                    **Deux cas d'usage :**
                    - **Nouveaux étudiants** : Créer l'étudiant puis l'inscrire (matricule vide)
                    - **Étudiants existants** : Inscrire un étudiant déjà enregistré (avec matricule)
                    
                    **Format CSV attendu :**
                    ```
                    Matricule (optionnel),Nom complet,Genre (MALE/FEMALE),Date de naissance (YYYY-MM-DD),Email,Téléphone
                    ,Jean Dupont,MALE,1995-03-15,jean@email.com,+225123456789
                    PI-CI-25A0001,Marie Kouassi,FEMALE,1996-07-22,marie@email.com,+225987654321
                    ```
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importation réussie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentImportResult.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides ou fichier corrompu"),
            @ApiResponse(responseCode = "404", description = "Offre ou classe non trouvée")
    })
    public ResponseEntity<StudentImportResult> importStudentsFromCsv(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID de l'offre à laquelle inscrire les étudiants", required = true)
            @RequestParam String offerId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) {

        log.info("Réception d'une demande d'importation CSV pour l'offre: {}, classe: {}",
                offerId, classroomId != null ? classroomId : "non spécifiée");

        StudentImportResult result = studentImportService.importStudentsFromCsv(file, offerId, classroomId);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des étudiants depuis un fichier Excel",
            description = """
                    Importe des étudiants depuis un fichier Excel et les inscrit à l'offre spécifiée.
                    
                    **Format Excel attendu :**
                    - Ligne 1 : Info offre (généré automatiquement par le template)
                    - Ligne 2 : Info classe (si applicable)
                    - Ligne 3/4 : En-têtes des colonnes
                    - Lignes suivantes : Données des étudiants
                    
                    **Conseil :** Utilisez le endpoint `/template` pour générer un fichier exemple.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importation réussie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentImportResult.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides ou fichier corrompu"),
            @ApiResponse(responseCode = "404", description = "Offre ou classe non trouvée")
    })
    public ResponseEntity<StudentImportResult> importStudentsFromExcel(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID de l'offre à laquelle inscrire les étudiants", required = true)
            @RequestParam String offerId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) {

        log.info("Réception d'une demande d'importation Excel pour l'offre: {}, classe: {}",
                offerId, classroomId != null ? classroomId : "non spécifiée");

        StudentImportResult result = studentImportService.importStudentsFromExcel(file, offerId, classroomId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/template")
    @Operation(summary = "Générer un template Excel",
            description = """
                    Génère un template Excel pour l'importation d'étudiants dans une offre spécifique.
                    
                    **Le template contient :**
                    - Informations sur l'offre et la classe
                    - En-têtes des colonnes avec format attendu
                    - Exemples de données pour nouveaux et anciens étudiants
                    
                    **Utilisation :**
                    1. Télécharger le template
                    2. Remplir avec les données des étudiants
                    3. Uploader via `/excel` ou `/csv`
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Offre ou classe non trouvée")
    })
    public ResponseEntity<Resource> generateExcelTemplate(
            @Parameter(description = "ID de l'offre", required = true)
            @RequestParam String offerId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) throws IOException {

        log.info("Génération d'un template Excel pour l'offre: {}, classe: {}",
                offerId, classroomId != null ? classroomId : "non spécifiée");

        byte[] templateBytes = studentImportService.generateExcelTemplate(offerId, classroomId);

        ByteArrayResource resource = new ByteArrayResource(templateBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-import-template.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(templateBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

}