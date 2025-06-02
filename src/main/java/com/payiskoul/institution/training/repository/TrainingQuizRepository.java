package com.payiskoul.institution.training.repository;

import com.payiskoul.institution.training.model.TrainingQuiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingQuizRepository extends MongoRepository<TrainingQuiz, String> {
    List<TrainingQuiz> findByParentIdAndParentType(String parentId, TrainingQuiz.ParentType parentType);
    void deleteByParentId(String parentId);
}