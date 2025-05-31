package com.payiskoul.institution.program.model;

/**
 * Types d'offres de formation supportés
 */
public enum OfferType {
    /**
     * Offre académique (ex : Licence, Master)
     * Année académique au format : 2024-2025
     * Peut avoir des classes (salles)
     */
    ACADEMIC,

    /**
     * Offre professionnelle (ex : Formation certifiante)
     * Année au format : 2024
     * Pas de classes, inscription directe
     */
    PROFESSIONAL
}