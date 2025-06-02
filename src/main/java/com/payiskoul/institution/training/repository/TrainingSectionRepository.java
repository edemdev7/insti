package com.payiskoul.institution.training.repository;

import com.payiskoul.institution.training.model.TrainingSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSectionRepository extends MongoRepository<TrainingSection, String> {
    List<TrainingSection> findByTrainingOfferIdOrderByOrder(String trainingOfferId);
    void deleteByTrainingOfferId(String trainingOfferId);
}