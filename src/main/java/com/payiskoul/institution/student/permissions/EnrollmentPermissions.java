package com.payiskoul.institution.student.permissions;

import com.payiskoul.institution.student.model.Enrollment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("enrollmentPermissions")
public class EnrollmentPermissions {

    /**
     * Vérifie si l'utilisateur peut voir l'inscription
     */
    public boolean canView(Authentication auth, Enrollment enrollment) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Admin peut tout voir
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        // L'étudiant peut voir ses propres inscriptions
        if (hasRole(auth, "STUDENT")) {
            String studentId = (String) auth.getDetails();
            return enrollment.getStudentId().equals(studentId);
        }

        // L'institution peut voir les inscriptions à ses offres
        if (hasRole(auth, "INSTITUTION")) {
            String institutionId = (String) auth.getDetails();
            return enrollment.getInstitutionId().equals(institutionId);
        }

        return false;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}