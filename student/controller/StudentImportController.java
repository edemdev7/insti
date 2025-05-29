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

@RestController
@RequestMapping("/v1/students/import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Importation d'étudiants", description = "API pour l'importation d'étudiants depuis des fichiers Excel ou CSV")
public class StudentImportController {

    private final StudentImportService studentImportService;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des étudiants depuis un fichier CSV",
            description = "Importe des étudiants depuis un fichier CSV et les inscrit au programme spécifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importation réussie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentImportResult.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides ou fichier corrompu"),
            @ApiResponse(responseCode = "404", description = "Programme ou classe non trouvé")
    })
    public ResponseEntity<StudentImportResult> importStudentsFromCsv(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID du programme auquel inscrire les étudiants", required = true)
            @RequestParam String programId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) {

        log.info("Réception d'une demande d'importation CSV pour le programme: {}, classe: {}",
                programId, classroomId != null ? classroomId : "non spécifiée");

        StudentImportResult result = studentImportService.importStudentsFromCsv(file, programId, classroomId);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des étudiants depuis un fichier Excel",
            description = "Importe des étudiants depuis un fichier Excel et les inscrit au programme spécifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Importation réussie",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentImportResult.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides ou fichier corrompu"),
            @ApiResponse(responseCode = "404", description = "Programme ou classe non trouvé")
    })
    public ResponseEntity<StudentImportResult> importStudentsFromExcel(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "ID du programme auquel inscrire les étudiants", required = true)
            @RequestParam String programId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) {

        log.info("Réception d'une demande d'importation Excel pour le programme: {}, classe: {}",
                programId, classroomId != null ? classroomId : "non spécifiée");

        StudentImportResult result = studentImportService.importStudentsFromExcel(file, programId, classroomId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/template")
    @Operation(summary = "Générer un template Excel",
            description = "Génère un template Excel pour l'importation d'étudiants dans un programme spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Programme ou classe non trouvé")
    })
    public ResponseEntity<Resource> generateExcelTemplate(
            @Parameter(description = "ID du programme", required = true)
            @RequestParam String programId,
            @Parameter(description = "ID de la classe (optionnel)")
            @RequestParam(required = false) String classroomId) throws IOException {

        log.info("Génération d'un template Excel pour le programme: {}, classe: {}",
                programId, classroomId != null ? classroomId : "non spécifiée");

        byte[] templateBytes = studentImportService.generateExcelTemplate(programId, classroomId);

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