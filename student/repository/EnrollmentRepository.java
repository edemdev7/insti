package com.payiskoul.institution.student.repository;

import com.payiskoul.institution.student.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByStudentId(String studentId);
    List<Enrollment> findByStudentIdAndAcademicYear(String studentId, String academicYear);
    boolean existsByStudentIdAndProgramLevelIdAndAcademicYear(String studentId, String programLevelId, String academicYear);

    List<Enrollment> findByClassroomId(String classroomId);
}