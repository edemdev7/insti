package com.payiskoul.institution.utils.generator;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";

    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un mot de passe sécurisé avec:
     * - 3 caractères majuscules
     * - 3 caractères minuscules
     * - 2 chiffres
     *
     * @return mot de passe généré
     */
    public static String generateSecurePassword() {
        // Générer les caractères spécifiques requis
        StringBuilder password = new StringBuilder(8);

        // Ajouter 3 majuscules
        for (int i = 0; i < 3; i++) {
            password.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        }

        // Ajouter 3 minuscules
        for (int i = 0; i < 3; i++) {
            password.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        }

        // Ajouter 2 chiffres
        for (int i = 0; i < 2; i++) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }

        // Mélanger les caractères pour rendre le mot de passe plus aléatoire
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int randomIndex = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[randomIndex];
            passwordArray[randomIndex] = temp;
        }

        return new String(passwordArray);
    }
}
