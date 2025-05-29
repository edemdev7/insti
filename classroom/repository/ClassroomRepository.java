package com.payiskoul.institution.classroom.repository;

import com.payiskoul.institution.classroom.model.Classroom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends MongoRepository<Classroom, String> {

    List<Classroom> findByInstitutionId(String institutionId);

    List<Classroom> findByProgramLevelId(String programLevelId);

    List<Classroom> findByInstitutionIdAndProgramLevelId(String institutionId, String programLevelId);

    Optional<Classroom> findByNameAndProgramLevelId(String name, String programLevelId);

    // Rechercher une classe avec de la capacité disponible
    // Utilisation d'une requête MongoDB personnalisée pour trouver une classe avec des places disponibles
    @Query("{ 'programLevelId': ?0, 'currentCount': { $lt: 'capacity' } }")
    Optional<Classroom> findFirstAvailableByProgramLevelId(String programLevelId);

    // Alternative utilisant la méthode de nommage standard de Spring Data
    Optional<Classroom> findFirstByProgramLevelIdAndCurrentCountLessThan(String programLevelId, int capacity);
}