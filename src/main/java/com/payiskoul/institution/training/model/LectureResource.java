package com.payiskoul.institution.training.model;

import lombok.*;

import java.io.Serializable; /**
 * Représente une ressource associée à une leçon
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureResource implements Serializable {
    /**
     * Nom de la ressource
     */
    private String name;

    /**
     * Type de ressource
     */
    private ResourceType type;

    /**
     * URL de la ressource
     */
    private String url;

    /**
     * Taille du fichier (en bytes)
     */
    private Long fileSize;

    /**
     * Description de la ressource
     */
    private String description;

    /**
     * Types de ressources
     */
    public enum ResourceType {
        PDF,
        WORD,
        EXCEL,
        POWERPOINT,
        IMAGE,
        AUDIO,
        VIDEO,
        ARCHIVE,
        LINK,
        OTHER
    }
}
