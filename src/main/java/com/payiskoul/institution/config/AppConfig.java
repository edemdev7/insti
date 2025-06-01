package com.payiskoul.institution.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@Profile({"default","dev","local"})
public class AppConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayiSkoul - API de gestion des institutions")
                        .version("1.0")
                        .description("API pour la gestion des institutions éducatives de la plateforme PayiSkoul")
                        .contact(new Contact()
                                .name("Équipe PayiSkoul")
                                .email("support@payiskoul.com"))
                );
    }
}