package com.payiskoul.institution.exception;

public enum ErrorCode {
    INSTITUTION_NOT_FOUND("Institution introuvable"),
    INSTITUTION_ALREADY_EXISTS("Institution déjà existante"),
    PROGRAM_LEVEL_ALREADY_EXISTS("Niveau de programme déjà existant"),
    PROGRAM_LEVEL_NOT_FOUND("Niveau de programme introuvable"),
    STUDENT_NOT_FOUND("Étudiant introuvable"),
    STUDENT_ALREADY_EXISTS("Étudiant déjà existant"),
    ENROLLMENT_ALREADY_EXISTS("Inscription déjà existante"),
    VALIDATION_FAILED("Validation des données échouée"),
    INTERNAL_SERVER_ERROR("Erreur interne du serveur"),
    UNSUPPORTED_METHOD("Method non supportée"),

    // Exceptions pour les classes
    CLASSROOM_NOT_FOUND("Classe introuvable"),
    CLASSROOM_ALREADY_EXISTS("Classe déjà existante"),
    CLASSROOM_FULL("Classe complète"),
    NO_CLASSROOM_AVAILABLE("Aucune classe disponible"),
    INVALID_INSTITUTION_PROGRAM("Programme invalide pour cette institution"),
    INVALID_CLASSROOM_PROGRAM("Classe invalide pour ce programme"),

    // Exceptions pour l'importation de fichiers
    INVALID_FILE_FORMAT("Format de fichier invalide"),
    FILE_PROCESSING_ERROR("Erreur lors du traitement du fichier"),
    MISSING_REQUIRED_COLUMNS("Colonnes requises manquantes"),
    IMPORT_DATA_VALIDATION_ERROR("Erreur de validation des données importées"),
    MAX_FILE_SIZE_EXCEEDED("Taille maximale du fichier dépassée"),
    UNSUPPORTED_FILE_TYPE("Type de fichier non supporté"),
    // Exceptions pour les validations de données
    INVALID_INPUT("Donnée d'entrée invalide"),
    DUPLICATE_DATA("Donnée en doublon"),
    INVALID_DATE_FORMAT("Format de date invalide"),
    // Nouveaux codes
    PAYMENT_ALREADY_PROCESSED("Paiement déjà traité"),
    DUPLICATE_REFERENCE("Référence de paiement dupliquée"),

    INVALID_PAYMENT_DATA("Données de paiement invalid"),
    SERVICE_TIMEOUT("Service indisponible"),
    USER_REGISTRATION_FAILED("Échec de l'enregistrement de l'utilisateur administrateur de l'institution");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}