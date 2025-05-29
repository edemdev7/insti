package com.payiskoul.institution.student.dto;

import com.payiskoul.institution.student.model.Gender;
import com.payiskoul.institution.student.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Données pour la création d'un nouvel étudiant")
public record CreateStudentRequest(
        @Schema(description = "Nom complet de l'étudiant", example = "Jean Dupont")
        @NotBlank(message = "Le nom complet est obligatoire")
        String fullName,

        @Schema(description = "Genre de l'étudiant (MALE ou FEMALE)", example = "MALE")
        @NotNull(message = "Le genre est obligatoire")
        Gender gender,

        @Schema(description = "Date de naissance de l'étudiant", example = "2001-05-14")
        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate birthDate,

        @Schema(description = "Email de l'étudiant", example = "jean.dupont@example.com")
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @Schema(description = "Numéro de téléphone de l'étudiant", example = "+2250701020304")
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        String phone,

        @Schema(description = "ID du programme auquel l'étudiant s'inscrit", example = "661be293e90f256128f7e203")
        @NotBlank(message = "L'ID du programme est obligatoire")
        String programId,

        @Schema(description = "ID de la classe à laquelle l'étudiant sera assigné (optionnel)", example = "661be293e90f256128f7e204")
        String classroomId
) {
}