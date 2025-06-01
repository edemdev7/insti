package com.payiskoul.institution.student.controller;

import com.payiskoul.institution.student.dto.CreateEnrollmentRequest;
import com.payiskoul.institution.student.dto.EnrollmentResponse;
import com.payiskoul.institution.student.service.EnrollmentService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inscriptions", description = "API pour la gestion des inscriptions d'étudiants")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "Créer une inscription", description = "Inscrit un étudiant à un programme pour une année académique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscription créée avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "404", description = "Étudiant ou programme non trouvé"),
            @ApiResponse(responseCode = "409", description = "L'étudiant est déjà inscrit à ce programme pour cette année académique")
    })
    public ResponseEntity<EnrollmentResponse> createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        log.info("Création d'une nouvelle inscription pour l'étudiant: {}", request.studentId());
        EnrollmentResponse response = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}