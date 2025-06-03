package com.payiskoul.institution.training.permissions;

import com.payiskoul.institution.program.model.TrainingOffer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("trainingOfferPermissions")
public class TrainingOfferPermissions {

    /**
     * Vérifie si l'utilisateur peut modifier l'offre (propriétaire ou admin)
     */
    public boolean canModify(Authentication auth, TrainingOffer offer) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Admin peut tout modifier
        if (hasRole(auth, "ADMIN")) {
            return true;
        }

        // Propriétaire de l'institution peut modifier ses offres
        if (hasRole(auth, "INSTITUTION")) {
            String institutionId = (String) auth.getDetails();
            return offer.getInstitutionId().equals(institutionId);
        }

        return false;
    }

    /**
     * Vérifie si l'utilisateur peut voir l'offre
     */
    public boolean canView(Authentication auth, TrainingOffer offer) {
        // Offres publiées et approuvées sont visibles par tous
        if (offer.getIsPublished() && offer.getIsApproved()) {
            return true;
        }

        // Sinon, même logique que canModify
        return canModify(auth, offer);
    }

    /**
     * Vérifie si l'utilisateur peut approuver l'offre (admin seulement)
     */
    public boolean canApprove(Authentication auth) {
        return auth != null && auth.isAuthenticated() && hasRole(auth, "ADMIN");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}