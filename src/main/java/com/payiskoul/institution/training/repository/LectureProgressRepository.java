package com.payiskoul.institution.training.repository;

import com.payiskoul.institution.training.model.LectureProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProgressRepository extends MongoRepository<LectureProgress, String> {
    List<LectureProgress> findByEnrollmentId(String enrollmentId);
    List<LectureProgress> findByLectureId(String lectureId);
    Optional<LectureProgress> findByEnrollmentIdAndLectureId(String enrollmentId, String lectureId);
    void deleteByEnrollmentId(String enrollmentId);
}