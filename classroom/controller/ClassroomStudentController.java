// com.payiskoul.institution.classroom.controller.ClassroomStudentController.java
package com.payiskoul.institution.classroom.controller;

import com.payiskoul.institution.classroom.service.ClassroomStudentService;
import com.payiskoul.institution.student.dto.StudentResponse;
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

import java.util.List;

@RestController
@RequestMapping("/v1/classrooms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Étudiants par classe", description = "API pour la gestion des étudiants par classe")
public class ClassroomStudentController {

    private final ClassroomStudentService classroomStudentService;

    @GetMapping("/{classroomId}/students")
    @Operation(summary = "Récupérer les étudiants d'une classe",
            description = "Récupère la liste des étudiants inscrits dans une classe spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des étudiants récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Classe non trouvée")
    })
    public ResponseEntity<List<StudentResponse>> getStudentsByClassroom(@PathVariable String classroomId) {
        log.info("Récupération des étudiants pour la classe: {}", classroomId);

        List<StudentResponse> students = classroomStudentService.getStudentsByClassroom(classroomId);
        return ResponseEntity.ok(students);
    }
}