package com.payiskoul.institution.student.dto;

import com.payiskoul.institution.student.model.Gender;
import com.payiskoul.institution.student.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Schema(description = "Informations complètes d'un étudiant")
public record StudentResponse(
        @Schema(description = "ID unique de l'étudiant", example = "6620f3fabcde123456789abc")
        String id,

        @Schema(description = "Matricule unique de l'étudiant", example = "PI-CI-25A0042")
        String matricule,

        @Schema(description = "Nom complet de l'étudiant", example = "Jean Dupont")
        String fullName,

        @Schema(description = "Genre de l'étudiant", example = "MALE")
        Gender gender,

        @Schema(description = "Date de naissance de l'étudiant", example = "2001-05-14")
        LocalDate birthDate,

        @Schema(description = "Email de l'étudiant", example = "jean.dupont@example.com")
        String email,

        @Schema(description = "Numéro de téléphone de l'étudiant", example = "+2250701020304")
        String phone,

        @Schema(description = "Date d'enregistrement de l'étudiant", example = "2025-04-18T09:30:00")
        LocalDateTime registeredAt,

        @Schema(description = "ID du programme auquel l'étudiant est inscrit", example = "661be293e90f256128f7e203")
        String programId
) {
}
