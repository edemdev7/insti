package com.payiskoul.institution.classroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payiskoul.institution.classroom.service.ClassroomStudentService;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ClassroomNotFound;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.student.dto.StudentResponse;
import com.payiskoul.institution.student.model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassroomStudentController.class)
class ClassroomStudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClassroomStudentService classroomStudentService;

    private List<StudentResponse> mockStudents;
    private final String CLASSROOM_ID = "classroom123";

    @BeforeEach
    void setUp() {
        // Création des données de test fixes
        LocalDateTime now = LocalDateTime.of(2025, 4, 18, 10, 30, 0);
        LocalDate birthDate1 = LocalDate.of(2000, 1, 1);
        LocalDate birthDate2 = LocalDate.of(2001, 2, 2);

        mockStudents = Arrays.asList(
                new StudentResponse(
                        "student1",
                        "PI-CI-25A0001",
                        "Jean Dupont",
                        Gender.MALE,
                        birthDate1,
                        "jean.dupont@example.com",
                        "+2250701020304",
                        now,
                        "program1"
                ),
                new StudentResponse(
                        "student2",
                        "PI-CI-25A0002",
                        "Marie Martin",
                        Gender.FEMALE,
                        birthDate2,
                        "marie.martin@example.com",
                        "+2250702030405",
                        now,
                        "program1"
                )
        );
    }

    @Test
    @DisplayName("Devrait retourner la liste des étudiants pour une classe existante")
    void getStudentsByClassroom_ShouldReturnStudentList() throws Exception {
        // Préparation
        when(classroomStudentService.getStudentsByClassroom(CLASSROOM_ID)).thenReturn(mockStudents);

        // Exécution et vérification
        mockMvc.perform(get("/v1/classrooms/{classroomId}/students", CLASSROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("student1")))
                .andExpect(jsonPath("$[0].matricule", is("PI-CI-25A0001")))
                .andExpect(jsonPath("$[0].fullName", is("Jean Dupont")))
                .andExpect(jsonPath("$[0].gender", is("MALE")))
                .andExpect(jsonPath("$[0].email", is("jean.dupont@example.com")))
                .andExpect(jsonPath("$[1].id", is("student2")))
                .andExpect(jsonPath("$[1].matricule", is("PI-CI-25A0002")))
                .andExpect(jsonPath("$[1].fullName", is("Marie Martin")))
                .andExpect(jsonPath("$[1].gender", is("FEMALE")));

        // Vérifier que le service a été appelé une fois avec le bon ID
        verify(classroomStudentService, times(1)).getStudentsByClassroom(CLASSROOM_ID);
    }

    @Test
    @DisplayName("Devrait retourner une liste vide si aucun étudiant n'est trouvé")
    void getStudentsByClassroom_ShouldReturnEmptyList() throws Exception {
        // Préparation
        when(classroomStudentService.getStudentsByClassroom(CLASSROOM_ID)).thenReturn(List.of());

        // Exécution et vérification
        mockMvc.perform(get("/v1/classrooms/{classroomId}/students", CLASSROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        // Vérifier que le service a été appelé une fois avec le bon ID
        verify(classroomStudentService, times(1)).getStudentsByClassroom(CLASSROOM_ID);
    }

    @Test
    @DisplayName("Devrait retourner 404 si la classe n'existe pas")
    void getStudentsByClassroom_ClassroomNotFound() throws Exception {
        // Préparation
        when(classroomStudentService.getStudentsByClassroom(CLASSROOM_ID))
                .thenThrow(new ClassroomNotFound( "Classe introuvable", Map.of("classroomId", CLASSROOM_ID)));

        // Exécution et vérification
        mockMvc.perform(get("/v1/classrooms/{classroomId}/students", CLASSROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("CLASSROOM_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Classe introuvable")))
                .andExpect(jsonPath("$.details.classroomId", is(CLASSROOM_ID)));

        verify(classroomStudentService, times(1)).getStudentsByClassroom(CLASSROOM_ID);
    }
}