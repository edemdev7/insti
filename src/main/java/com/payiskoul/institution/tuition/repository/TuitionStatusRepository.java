package com.payiskoul.institution.tuition.repository;

import com.payiskoul.institution.tuition.model.TuitionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuitionStatusRepository extends MongoRepository<TuitionStatus, String> {
    List<TuitionStatus> findByMatricule(String matricule);
    List<TuitionStatus> findByEnrollmentId(String enrollmentId);
}