package com.payiskoul.institution.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // La configuration par défaut de Spring Boot est suffisante pour notre cas d'usage simple
    // Cela permet aux méthodes annotées avec @Async de s'exécuter dans un thread séparé
}