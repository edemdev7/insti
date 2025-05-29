package com.payiskoul.institution.program.repository;

import com.payiskoul.institution.program.model.ProgramLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramLevelRepository extends MongoRepository<ProgramLevel, String> {

    // Méthodes existantes sans pagination
    List<ProgramLevel> findByInstitutionId(String institutionId);
    List<ProgramLevel> findByInstitutionIdAndAcademicYear(String institutionId, String academicYear);
    List<ProgramLevel> findByInstitutionIdAndNameAndAcademicYear(
            String institutionId, String name, String academicYear);
    boolean existsByInstitutionIdAndNameAndAcademicYear(
            String institutionId, String name, String academicYear);

    // Nouvelles méthodes avec pagination
    Page<ProgramLevel> findByInstitutionId(String institutionId, Pageable pageable);
    Page<ProgramLevel> findByInstitutionIdAndAcademicYear(String institutionId, String academicYear, Pageable pageable);

    /**
     * Récupère le nom d'une institution par son ID
     */
    @Query(value = "{ '_id': ?0 }", fields = "{ 'name': 1 }")
    String findInstitutionNameById(String institutionId);
}