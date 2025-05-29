package com.payiskoul.institution.student.controller;

import com.payiskoul.institution.student.dto.CreateStudentRequest;
import com.payiskoul.institution.student.dto.StudentResponse;
import com.payiskoul.institution.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/v1/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Étudiants", description = "API pour la gestion des étudiants")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Créer un étudiant", description = "Crée un nouvel étudiant et l'inscrit au programme spécifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Étudiant créé avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "404", description = "Programme non trouvé"),
            @ApiResponse(responseCode = "409", description = "Un étudiant avec cet email existe déjà")
    })
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        log.info("Création d'un nouvel étudiant avec le nom: {}", request.fullName());
        StudentResponse response = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{matricule}")
    @Operation(summary = "Récupérer un étudiant", description = "Récupère les informations d'un étudiant par son matricule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étudiant trouvé",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Étudiant non trouvé")
    })
    public ResponseEntity<StudentResponse> getStudent(@PathVariable String matricule) {
        log.info("Récupération de l'étudiant avec le matricule: {}", matricule);
        StudentResponse response = studentService.getStudentByMatricule(matricule);
        return ResponseEntity.ok(response);
    }
}