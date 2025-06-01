package com.payiskoul.institution.student.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service de génération de matricules pour les étudiants
 * Format: PI-XX-25A0123
 * PI : Identifiant de la plateforme PayIskoul
 * XX : Code pays (ex: CI pour Côte d'Ivoire)
 * 25 : Année d'enregistrement (ex. 2025)
 * A : Lettre qui change lorsqu'on atteint la limite de combinaisons
 * 0123 : Numéro séquentiel sur 4 chiffres (avec zéros initiaux)
 */
@Component
public class MatriculeGenerator {

    private static final String PLATFORM_ID = "PI";
    private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int MAX_SEQUENCE = 10000; // 0000-9999

    private final MongoTemplate mongoTemplate;
    private final Object lock = new Object(); // Pour la synchronisation

    public MatriculeGenerator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Génère un nouveau matricule avec le code pays spécifié
     * @param countryCode code du pays (ex: CI pour Côte d'Ivoire)
     * @return un matricule unique au format PI-XX-25A0123
     */
    public String generateMatricule(String countryCode) {
        // S'assurer que le code pays est en majuscules et limité à 2 caractères
        String normalizedCountryCode = normalizeCountryCode(countryCode);

        synchronized (lock) {
            // Récupérer l'année courante (25 pour 2025)
            String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);

            // Récupérer le dernier matricule généré pour cette année et ce pays
            String sequenceId = year + "-" + normalizedCountryCode;
            Optional<MatriculeSequence> currentSequence = findCurrentSequence(sequenceId);

            if (currentSequence.isEmpty()) {
                // Premier matricule de l'année pour ce pays, commencer par A0000
                MatriculeSequence newSequence = new MatriculeSequence(sequenceId, 0, 0);
                mongoTemplate.save(newSequence);
                return formatMatricule(normalizedCountryCode, year, 0, 0);
            } else {
                MatriculeSequence sequence = currentSequence.get();
                int letterIndex = sequence.getLetterIndex();
                int numberSequence = sequence.getNumberSequence();

                // Incrémenter la séquence
                numberSequence++;

                // Si on atteint la limite de la séquence numérique, passer à la lettre suivante
                if (numberSequence >= MAX_SEQUENCE) {
                    letterIndex++;
                    numberSequence = 0;

                    // Vérifier si on a épuisé toutes les lettres
                    if (letterIndex >= LETTERS.length) {
                        throw new RuntimeException("Toutes les combinaisons de matricules ont été épuisées pour cette année et ce pays.");
                    }
                }

                // Mettre à jour la séquence en base de données
                sequence.setLetterIndex(letterIndex);
                sequence.setNumberSequence(numberSequence);
                mongoTemplate.save(sequence);

                return formatMatricule(normalizedCountryCode, year, letterIndex, numberSequence);
            }
        }
    }

    /**
     * Normalise le code pays (majuscules, limité à 2 caractères)
     */
    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return "CI"; // Code par défaut
        }

        String normalized = countryCode.toUpperCase().trim();

        // Limiter à 2 caractères si plus long
        if (normalized.length() > 2) {
            normalized = normalized.substring(0, 2);
        }

        // Compléter avec un "X" si un seul caractère
        if (normalized.length() == 1) {
            normalized += "X";
        }

        return normalized;
    }

    /**
     * Formate le matricule avec les valeurs données
     */
    private String formatMatricule(String countryCode, String year, int letterIndex, int numberSequence) {
        char letter = LETTERS[letterIndex];
        String number = String.format("%04d", numberSequence);
        return String.format("%s-%s-%s%s%s", PLATFORM_ID, countryCode, year, letter, number);
    }

    /**
     * Récupère la séquence actuelle pour l'année et le pays donnés
     */
    private Optional<MatriculeSequence> findCurrentSequence(String sequenceId) {
        Query query = new Query(Criteria.where("sequenceId").is(sequenceId));
        return Optional.ofNullable(mongoTemplate.findOne(query, MatriculeSequence.class));
    }

    /**
     * Classe interne pour stocker la séquence de génération des matricules en base de données
     */
    private static class MatriculeSequence {
        private String id;
        private String sequenceId; // Format: "année-pays" (ex: "25-CI")
        private int letterIndex;
        private int numberSequence;

        public MatriculeSequence() {
        }

        public MatriculeSequence(String sequenceId, int letterIndex, int numberSequence) {
            this.sequenceId = sequenceId;
            this.letterIndex = letterIndex;
            this.numberSequence = numberSequence;
        }

        // Getters et setters

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSequenceId() {
            return sequenceId;
        }

        public void setSequenceId(String sequenceId) {
            this.sequenceId = sequenceId;
        }

        public int getLetterIndex() {
            return letterIndex;
        }

        public void setLetterIndex(int letterIndex) {
            this.letterIndex = letterIndex;
        }

        public int getNumberSequence() {
            return numberSequence;
        }

        public void setNumberSequence(int numberSequence) {
            this.numberSequence = numberSequence;
        }
    }
}