// com.payiskoul.institution.classroom.service.ClassroomStudentService.java
package com.payiskoul.institution.classroom.service;

import com.payiskoul.institution.classroom.model.Classroom;
import com.payiskoul.institution.classroom.repository.ClassroomRepository;
import com.payiskoul.institution.exception.BusinessException;
import com.payiskoul.institution.exception.ClassroomNotFound;
import com.payiskoul.institution.exception.ErrorCode;
import com.payiskoul.institution.student.dto.StudentResponse;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomStudentService {

    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    /**
     * Récupère tous les étudiants inscrits dans une classe spécifique
     * @param classroomId ID de la classe
     * @return la liste des étudiants
     */
    //@Cacheable(value = "classroomStudents", key = "#classroomId")
    public List<StudentResponse> getStudentsByClassroom(String classroomId) {
        log.info("Récupération des étudiants pour la classe {}", classroomId);

        // Vérifier que la classe existe
        classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFound( "Classe introuvable", Map.of("classroomId", classroomId)));

        // Récupérer toutes les inscriptions pour cette classe
        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(classroomId);

        log.info("Nombre d'inscriptions trouvées pour la classe {}: {}", classroomId, enrollments.size());

        // Transformer les inscriptions en réponses d'étudiants
        return enrollments.stream()
                .map(enrollment -> {
                    // Récupérer l'étudiant associé à l'inscription
                    Student student = studentRepository.findById(enrollment.getStudentId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND,
                                    "Étudiant introuvable",
                                    Map.of("studentId", enrollment.getStudentId())));

                    // Convertir en DTO de réponse
                    return new StudentResponse(
                            student.getId(),
                            student.getMatricule(),
                            student.getFullName(),
                            student.getGender(),
                            student.getBirthDate(),
                            student.getEmail(),
                            student.getPhone(),
                            student.getRegisteredAt(),
                            enrollment.getProgramLevelId()
                    );
                })
                .toList();
    }
}