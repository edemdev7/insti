package com.payiskoul.institution.classroom.permissions;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("classroomPermissions")
public class ClassroomPermissions {

    /**
     * Vérifie si l'utilisateur peut modifier la classe
     */
    public boolean canModify(Authentication auth, String institutionId) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Admin peut tout modifier
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        // Propriétaire de l'institution peut modifier ses classes
        if (hasRole(auth, "INSTITUTION")) {
            String userInstitutionId = (String) auth.getDetails();
            return institutionId.equals(userInstitutionId);
        }

        return false;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}