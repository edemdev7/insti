package com.payiskoul.institution.training.repository;

import com.payiskoul.institution.training.model.TrainingLecture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingLectureRepository extends MongoRepository<TrainingLecture, String> {
    List<TrainingLecture> findBySectionIdOrderByOrder(String sectionId);
    void deleteBySectionId(String sectionId);
}