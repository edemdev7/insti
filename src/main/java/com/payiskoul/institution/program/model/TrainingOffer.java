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
 * Équivalent du modèle Course de Django
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
     */
    private String code;

    /**
     * Libellé de l'offre (équivalent title dans Django)
     */
    private String label;

    /**
     * Sous-titre de l'offre
     */
    private String subtitle;

    /**
     * Type d'offre: ACADEMIC ou PROFESSIONAL
     */
    private OfferType offerType;

    /**
     * Description détaillée de l'offre
     */
    private String description;

    /**
     * Image de couverture
     */
    private String coverImage;

    /**
     * Vidéo promotionnelle
     */
    private String promotionalVideo;

    /**
     * Modèle de tarification (FREE ou PAID)
     */
    @Builder.Default
    private PricingModel pricingModel = PricingModel.FREE;

    /**
     * Montant des frais de formation
     */
    @Builder.Default
    private BigDecimal tuitionAmount = BigDecimal.ZERO;

    /**
     * Devise des frais
     */
    @Builder.Default
    private String currency = "XOF";

    /**
     * Durée de la formation
     */
    private int duration;

    /**
     * Unité de durée (YEAR, MONTH, DAY, HOUR)
     */
    private DurationUnit durationUnit;

    /**
     * Type de certification délivrée
     */
    private String certification;

    /**
     * Année académique
     */
    private String academicYear;

    /**
     * Langue de l'offre
     */
    @Builder.Default
    private String language = "Français";

    /**
     * ID de l'institution propriétaire
     */
    private String institutionId;

    // === CHAMPS SPÉCIFIQUES AUX OFFRES PROFESSIONNELLES ===

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

    /**
     * Date limite de dernière inscription
     */
    private LocalDateTime lastEnrollmentDate;

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

    /**
     * Vérifie si l'offre est gratuite
     */
    public boolean isFree() {
        return this.pricingModel == PricingModel.FREE ||
                this.tuitionAmount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Vérifie si l'offre est payante
     */
    public boolean isPaid() {
        return this.pricingModel == PricingModel.PAID &&
                this.tuitionAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Modèles de tarification
     */
    public enum PricingModel {
        FREE,   // Gratuit
        PAID    // Payant
    }
}