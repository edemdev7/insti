package com.payiskoul.institution.student.repository;

import com.payiskoul.institution.student.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByMatricule(String matricule);
    boolean existsByEmail(String email);

    boolean existsByMatricule(String matricule);
}