package com.payiskoul.institution.reports.service;

import com.payiskoul.institution.program.model.TrainingOffer;
import com.payiskoul.institution.program.repository.TrainingOfferRepository;
import com.payiskoul.institution.student.model.Enrollment;
import com.payiskoul.institution.student.model.Student;
import com.payiskoul.institution.student.repository.EnrollmentRepository;
import com.payiskoul.institution.student.repository.StudentRepository;
import com.payiskoul.institution.training.model.LectureProgress;
import com.payiskoul.institution.training.repository.LectureProgressRepository;
import com.payiskoul.institution.tuition.model.TuitionStatus;
import com.payiskoul.institution.tuition.repository.TuitionStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service équivalent aux fonctions generate_course_report et generate_student_report de Django
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TrainingOfferRepository trainingOfferRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final LectureProgressRepository lectureProgressRepository;
    private final TuitionStatusRepository tuitionStatusRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Génère un rapport CSV pour une offre de formation
     * Équivalent de generate_course_report de Django
     */
    public String generateOfferReport(String offerId) {
        log.info("Génération du rapport pour l'offre: {}", offerId);

        TrainingOffer offer = trainingOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable: " + offerId));

        StringWriter csvWriter = new StringWriter();

        // En-têtes CSV
        csvWriter.append("Matricule,Nom Complet,Email,Date Inscription,Statut Inscription,")
                .append("Progression (%),Leçons Complétées,Temps Total (min),")
                .append("Statut Paiement,Montant Payé,Montant Restant,Dernière Activité\n");

        // Récupérer toutes les inscriptions pour cette offre
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // TODO: Corriger cette méthode
        List<Enrollment> offerEnrollments = enrollments.stream()
                .filter(e -> e.getProgramLevelId().equals(offerId))
                .collect(Collectors.toList());

        for (Enrollment enrollment : offerEnrollments) {
            try {
                // Récupérer l'étudiant
                Student student = studentRepository.findById(enrollment.getStudentId())
                        .orElse(null);

                if (student == null) continue;

                // Récupérer la progression
                List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollment.getId());

                double progressPercent = 0.0;
                long completedLectures = 0;
                int totalTimeSpent = 0;
                String lastActivity = "";

                if (!progressList.isEmpty()) {
                    progressPercent = progressList.stream()
                            .mapToInt(LectureProgress::getProgressPercent)
                            .average()
                            .orElse(0.0);

                    completedLectures = progressList.stream()
                            .filter(LectureProgress::getIsCompleted)
                            .count();

                    totalTimeSpent = progressList.stream()
                            .mapToInt(LectureProgress::getTimeSpent)
                            .sum();

                    lastActivity = progressList.stream()
                            .filter(p -> p.getLastAccessedAt() != null)
                            .map(p -> p.getLastAccessedAt().format(DATE_FORMATTER))
                            .findFirst()
                            .orElse("");
                }

                // Récupérer les informations de paiement
                List<TuitionStatus> tuitionStatuses = tuitionStatusRepository.findByEnrollmentId(enrollment.getId());
                String paymentStatus = "UNKNOWN";
                String amountPaid = "0";
                String amountRemaining = "0";

                if (!tuitionStatuses.isEmpty()) {
                    TuitionStatus status = tuitionStatuses.get(0);
                    paymentStatus = status.getPaymentStatus().name();
                    amountPaid = status.getPaidAmount().toString();
                    amountRemaining = status.getRemainingAmount().toString();
                }

                // Écrire la ligne CSV
                csvWriter.append(escapeCsv(student.getMatricule())).append(",")
                        .append(escapeCsv(student.getFullName())).append(",")
                        .append(escapeCsv(student.getEmail())).append(",")
                        .append(enrollment.getEnrolledAt().format(DATE_FORMATTER)).append(",")
                        .append(enrollment.getStatus().name()).append(",")
                        .append(String.format("%.1f", progressPercent)).append(",")
                        .append(String.valueOf(completedLectures)).append(",")
                        .append(String.valueOf(totalTimeSpent / 60)).append(",") // Convertir en minutes
                        .append(paymentStatus).append(",")
                        .append(amountPaid).append(",")
                        .append(amountRemaining).append(",")
                        .append(lastActivity).append("\n");

            } catch (Exception e) {
                log.error("Erreur lors de la génération du rapport pour l'inscription: {}", enrollment.getId(), e);
            }
        }

        log.info("Rapport généré pour l'offre {}: {} lignes", offerId, offerEnrollments.size());
        return csvWriter.toString();
    }

    /**
     * Génère un rapport CSV des étudiants pour une institution
     * Équivalent de generate_student_report de Django
     */
    public String generateStudentReport(String institutionId) {
        log.info("Génération du rapport des étudiants pour l'institution: {}", institutionId);

        StringWriter csvWriter = new StringWriter();

        // En-têtes CSV
        csvWriter.append("Matricule,Nom Complet,Genre,Date Naissance,Email,Téléphone,")
                .append("Date Inscription,Nombre Cours,Cours Actifs,Progression Moyenne (%),")
                .append("Statut Paiement Global,Total Payé,Total Restant\n");

        // Récupérer toutes les inscriptions de l'institution
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // TODO: Corriger
        Map<String, List<Enrollment>> studentEnrollments = enrollments.stream()
                .filter(e -> e.getInstitutionId().equals(institutionId))
                .collect(Collectors.groupingBy(Enrollment::getStudentId));

        for (Map.Entry<String, List<Enrollment>> entry : studentEnrollments.entrySet()) {
            try {
                String studentId = entry.getKey();
                List<Enrollment> studentEnrollmentList = entry.getValue();

                // Récupérer l'étudiant
                Student student = studentRepository.findById(studentId).orElse(null);
                if (student == null) continue;

                // Calculer les statistiques
                int totalCourses = studentEnrollmentList.size();
                long activeCourses = studentEnrollmentList.stream()
                        .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                        .count();

                // Progression moyenne
                double averageProgress = 0.0;
                for (Enrollment enrollment : studentEnrollmentList) {
                    List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollment.getId());
                    if (!progressList.isEmpty()) {
                        double enrollmentProgress = progressList.stream()
                                .mapToInt(LectureProgress::getProgressPercent)
                                .average()
                                .orElse(0.0);
                        averageProgress += enrollmentProgress;
                    }
                }
                averageProgress = totalCourses > 0 ? averageProgress / totalCourses : 0.0;

                // Informations de paiement globales
                List<TuitionStatus> allTuitionStatuses = tuitionStatusRepository.findByMatricule(student.getMatricule());
                String globalPaymentStatus = calculateGlobalPaymentStatus(allTuitionStatuses);
                String totalPaid = allTuitionStatuses.stream()
                        .map(TuitionStatus::getPaidAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                        .toString();
                String totalRemaining = allTuitionStatuses.stream()
                        .map(TuitionStatus::getRemainingAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                        .toString();

                // Écrire la ligne CSV
                csvWriter.append(escapeCsv(student.getMatricule())).append(",")
                        .append(escapeCsv(student.getFullName())).append(",")
                        .append(student.getGender().name()).append(",")
                        .append(student.getBirthDate().toString()).append(",")
                        .append(escapeCsv(student.getEmail())).append(",")
                        .append(escapeCsv(student.getPhone())).append(",")
                        .append(student.getRegisteredAt().format(DATE_FORMATTER)).append(",")
                        .append(String.valueOf(totalCourses)).append(",")
                        .append(String.valueOf(activeCourses)).append(",")
                        .append(String.format("%.1f", averageProgress)).append(",")
                        .append(globalPaymentStatus).append(",")
                        .append(totalPaid).append(",")
                        .append(totalRemaining).append("\n");

            } catch (Exception e) {
                log.error("Erreur lors de la génération du rapport pour l'étudiant: {}", entry.getKey(), e);
            }
        }

        log.info("Rapport des étudiants généré pour l'institution {}: {} étudiants",
                institutionId, studentEnrollments.size());
        return csvWriter.toString();
    }

    /**
     * Génère un rapport de progression détaillé pour une offre
     */
    public String generateProgressReport(String offerId) {
        log.info("Génération du rapport de progression pour l'offre: {}", offerId);

        StringWriter csvWriter = new StringWriter();

        // En-têtes CSV
        csvWriter.append("Matricule,Nom Étudiant,Leçon ID,Titre Leçon,Section,")
                .append("Progression (%),Terminé,Temps Passé (min),Dernière Consultation,Date Completion\n");

        // Récupérer toutes les inscriptions pour cette offre
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(""); // TODO: Corriger
        List<Enrollment> offerEnrollments = enrollments.stream()
                .filter(e -> e.getProgramLevelId().equals(offerId))
                .collect(Collectors.toList());

        for (Enrollment enrollment : offerEnrollments) {
            try {
                Student student = studentRepository.findById(enrollment.getStudentId()).orElse(null);
                if (student == null) continue;

                List<LectureProgress> progressList = lectureProgressRepository.findByEnrollmentId(enrollment.getId());

                for (LectureProgress progress : progressList) {
                    csvWriter.append(escapeCsv(student.getMatricule())).append(",")
                            .append(escapeCsv(student.getFullName())).append(",")
                            .append(progress.getLectureId()).append(",")
                            .append("Leçon").append(",") // TODO: Récupérer le vrai titre
                            .append("Section").append(",") // TODO: Récupérer la vraie section
                            .append(String.valueOf(progress.getProgressPercent())).append(",")
                            .append(progress.getIsCompleted() ? "Oui" : "Non").append(",")
                            .append(String.valueOf(progress.getTimeSpent() / 60)).append(",")
                            .append(progress.getLastAccessedAt() != null ?
                                    progress.getLastAccessedAt().format(DATE_FORMATTER) : "").append(",")
                            .append(progress.getCompletedAt() != null ?
                                    progress.getCompletedAt().format(DATE_FORMATTER) : "").append("\n");
                }

            } catch (Exception e) {
                log.error("Erreur lors de la génération du rapport de progression: {}", enrollment.getId(), e);
            }
        }

        return csvWriter.toString();
    }

    /**
     * Calcule le statut de paiement global d'un étudiant
     */
    private String calculateGlobalPaymentStatus(List<TuitionStatus> tuitionStatuses) {
        if (tuitionStatuses.isEmpty()) {
            return "UNKNOWN";
        }

        boolean hasUnpaid = tuitionStatuses.stream()
                .anyMatch(s -> s.getPaymentStatus().name().equals("UNPAID"));
        boolean hasPartial = tuitionStatuses.stream()
                .anyMatch(s -> s.getPaymentStatus().name().equals("PARTIALLY_PAID"));
        boolean allPaid = tuitionStatuses.stream()
                .allMatch(s -> s.getPaymentStatus().name().equals("PAID"));

        if (allPaid) return "FULLY_PAID";
        if (hasUnpaid && !hasPartial) return "UNPAID";
        return "PARTIALLY_PAID";
    }

    /**
     * Échappe les caractères spéciaux pour CSV
     */
    private String escapeCsv(String value) {
        if (value == null) return "";

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}