package com.payiskoul.institution.program.model;

import com.payiskoul.institution.program.dto.DurationUnit;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité unifiée représentant une offre de formation
 * Supporte à la fois les offres académiques et professionnelles
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "training_offers")
@CompoundIndex(name = "idx_institution_label_year",
        def = "{'institutionId': 1, 'label': 1, 'academicYear': 1}",
        unique = true)
public class TrainingOffer implements Serializable {
    @Id
    private String id;

    /**
     * Code généré automatiquement pour l'offre
     * Format: TYPE_LEVEL-INSTITUTION_ACRONYM-YEAR (ex: LIC1-ESATIC-2025)
     */
    private String code;

    /**
     * Libellé de l'offre (ex : "Licence 1 Informatique", "Formation DevOps")
     */
    private String label;

    /**
     * Type d'offre: ACADEMIC ou PROFESSIONAL
     */
    private OfferType offerType;

    /**
     * Description détaillée de l'offre
     */
    private String description;

    /**
     * Durée de la formation
     */
    private int duration;

    /**
     * Unité de durée (YEAR, MONTH, DAY, HOUR)
     */
    private DurationUnit durationUnit;

    /**
     * Montant des frais de formation
     */
    private BigDecimal tuitionAmount;

    /**
     * Devise des frais
     */
    @Builder.Default
    private String currency = "XOF";

    /**
     * Type de certification délivrée
     */
    private String certification;

    /**
     * Année académique (format : 2024-2025 pour ACADEMIC, 2024 pour PROFESSIONAL)
     */
    private String academicYear;

    /**
     * ID de l'institution propriétaire
     */
    private String institutionId;

    /**
     * Date limite de dernière inscription
     */
    private LocalDateTime lastEnrollmentDate;

    // === CHAMPS SPÉCIFIQUES AUX OFFRES PROFESSIONNELLES ===

    /**
     * Image de couverture de l'offre
     */
    private String coverImage;

    /**
     * Vidéo promotionnelle
     */
    private String promotionalVideo;

    /**
     * Statut de publication
     */
    @Builder.Default
    private Boolean isPublished = false;

    /**
     * Statut d'approbation
     */
    @Builder.Default
    private Boolean isApproved = false;

    /**
     * Date d'approbation
     */
    private LocalDateTime approvalDate;

    /**
     * Langue de l'offre
     */
    @Builder.Default
    private String language = "Français";

    /**
     * Prérequis pour l'offre
     */
    private String prerequisites;

    /**
     * Objectifs d'apprentissage
     */
    private String learningObjectives;

    /**
     * Public cible
     */
    private String targetAudience;

    /**
     * Modalités d'évaluation
     */
    private String assessmentMethods;

    /**
     * Ressources incluses
     */
    private String includedResources;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // === MÉTHODES UTILITAIRES ===

    /**
     * Méthode de compatibilité - retourne le label comme nom
     */
    public String getName() {
        return this.label;
    }

    /**
     * Méthode de compatibilité - définit le label comme nom
     */
    public void setName(String name) {
        this.label = name;
    }

    /**
     * Méthode de compatibilité pour les frais sous forme d'objet Tuition
     */
    public Tuition getTuition() {
        return Tuition.builder()
                .amount(this.tuitionAmount)
                .currency(this.currency)
                .build();
    }

    /**
     * Méthode de compatibilité pour définir les frais
     */
    public void setTuition(Tuition tuition) {
        if (tuition != null) {
            this.tuitionAmount = tuition.getAmount();
            this.currency = tuition.getCurrency();
        }
    }

    /**
     * Approuve l'offre par un administrateur
     */
    public void approveOffer(String adminUser) {
        this.isApproved = true;
        this.approvalDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcule le nombre total d'étudiants inscrits
     */
    public int getTotalStudents() {
        // Cette méthode sera implémentée via le service
        return 0;
    }

    /**
     * Vérifie si l'offre est une formation professionnelle
     */
    public boolean isProfessionalOffer() {
        return this.offerType == OfferType.PROFESSIONAL;
    }

    /**
     * Vérifie si l'offre est une formation académique
     */
    public boolean isAcademicOffer() {
        return this.offerType == OfferType.ACADEMIC;
    }
}