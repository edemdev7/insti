package com.payiskoul.institution.student.dto;


import com.payiskoul.institution.student.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(description = "Réponse d'inscription à une offre")
public record EnrollmentResponse(
        @Schema(description = "ID de l'inscription", example = "enroll-001")
        String id,

        @Schema(description = "Informations de l'étudiant")
        StudentInfo student,

        @Schema(description = "Informations de l'offre")
        OfferInfo offer,

        @Schema(description = "ID de l'institution", example = "664f82a9e9d034f2fca9b0e2")
        String institutionId,

        @Schema(description = "ID de la classe assignée", example = "664f82a9e9d034f2fca9b0e1")
        String classroomId,

        @Schema(description = "Date d'inscription", example = "2024-05-25T09:30:00Z")
        LocalDateTime enrolledAt,

        @Schema(description = "Statut de l'inscription", example = "ENROLLED")
        Enrollment.EnrollmentStatus status
) {
    // Constructeur compact qui appelle le constructeur canonique
    public EnrollmentResponse(String id, String studentId, String programLevelId, String academicYear,
                              Enrollment.EnrollmentStatus status, LocalDateTime enrolledAt) {
        this(
                id,
                new StudentInfo(studentId, null, null),  // Créer un StudentInfo avec l'ID uniquement
                new OfferInfo(programLevelId, academicYear), // Créer un OfferInfo basique
                null, // institutionId
                null, // classroomId
                enrolledAt,
                status
        );
    }

}