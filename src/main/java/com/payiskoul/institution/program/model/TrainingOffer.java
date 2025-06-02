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
 * Équivalent du modèle Course de Django avec support complet des formations professionnelles
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

    /**
     * Niveau de difficulté (BEGINNER, INTERMEDIATE, ADVANCED)
     */
    private DifficultyLevel difficultyLevel;

    /**
     * Tags/mots-clés pour faciliter la recherche
     */
    private String[] tags;

    /**
     * Nombre maximum d'étudiants
     */
    private Integer maxStudents;

    /**
     * Support fourni (FORUM, EMAIL, LIVE_CHAT, NONE)
     */
    private SupportType supportType;

    /**
     * Certificat automatique à la fin
     */
    @Builder.Default
    private Boolean automaticCertificate = false;

    /**
     * Score minimum pour obtenir le certificat (en %)
     */
    @Builder.Default
    private Integer minimumScore = 70;

    /**
     * Offre recommandée/mise en avant
     */
    @Builder.Default
    private Boolean isFeatured = false;

    /**
     * Note moyenne des évaluations
     */
    @Builder.Default
    private Double averageRating = 0.0;

    /**
     * Nombre total d'évaluations
     */
    @Builder.Default
    private Integer totalReviews = 0;

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
     * Vérifie si l'offre accepte encore des inscriptions
     */
    public boolean isEnrollmentOpen() {
        if (!isPublished || !isApproved) {
            return false;
        }

        if (lastEnrollmentDate != null && LocalDateTime.now().isAfter(lastEnrollmentDate)) {
            return false;
        }

        // Vérifier le nombre maximum d'étudiants si défini
        if (maxStudents != null && getTotalStudents() >= maxStudents) {
            return false;
        }

        return true;
    }

    /**
     * Met à jour la note moyenne
     */
    public void updateRating(double newRating) {
        if (totalReviews == 0) {
            this.averageRating = newRating;
            this.totalReviews = 1;
        } else {
            double totalScore = this.averageRating * this.totalReviews;
            this.totalReviews++;
            this.averageRating = (totalScore + newRating) / this.totalReviews;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // === ENUMS ===

    /**
     * Modèles de tarification
     */
    public enum PricingModel {
        FREE,   // Gratuit
        PAID    // Payant
    }

    /**
     * Niveaux de difficulté
     */
    @Getter
    public enum DifficultyLevel {
        BEGINNER("Débutant"),
        INTERMEDIATE("Intermédiaire"),
        ADVANCED("Avancé"),
        EXPERT("Expert");

        private final String displayName;

        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }

    }

    /**
     * Types de support
     */
    @Getter
    public enum SupportType {
        NONE("Aucun support"),
        FORUM("Forum communautaire"),
        EMAIL("Support par email"),
        LIVE_CHAT("Chat en direct"),
        VIDEO_CALL("Appels vidéo"),
        FULL("Support complet");

        private final String displayName;

        SupportType(String displayName) {
            this.displayName = displayName;
        }

    }
}