package com.payiskoul.institution.classroom.controller;

import com.payiskoul.institution.classroom.dto.ClassroomList;
import com.payiskoul.institution.classroom.dto.ClassroomResponse;
import com.payiskoul.institution.classroom.dto.CreateClassroomRequest;
import com.payiskoul.institution.classroom.service.ClassroomService;
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

import java.util.List;

@RestController
@RequestMapping("/v1/institutions/{institutionId}/programs/{programId}/classrooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classes", description = "API pour la gestion des classes")
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    @Operation(summary = "Créer une classe", description = "Crée une nouvelle classe pour un programme spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Classe créée avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassroomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données d'entrée invalides"),
            @ApiResponse(responseCode = "404", description = "Institution ou programme introuvable")
    })
    public ResponseEntity<ClassroomResponse> createClassroom(
            @PathVariable String institutionId,
            @PathVariable String programId,
            @Valid @RequestBody CreateClassroomRequest request) {

        log.info("Création d'une nouvelle classe pour l'institution {} et le programme {}: {}",
                institutionId, programId, request.name());

        ClassroomResponse response = classroomService.createClassroom(institutionId, programId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Récupérer les classes", description = "Récupère toutes les classes pour un programme spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classes récupérées avec succès",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassroomList.class))),
            @ApiResponse(responseCode = "404", description = "Institution ou programme introuvable")
    })
    public ResponseEntity<ClassroomList> getClassrooms(
            @PathVariable String institutionId,
            @PathVariable String programId) {

        log.info("Récupération des classes pour l'institution {} et le programme {}",
                institutionId, programId);

        ClassroomList response = classroomService.getClassroomsByProgram(institutionId, programId);
        return ResponseEntity.ok(response);
    }
}