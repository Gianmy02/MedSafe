package it.unisa.project.medsafe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev", "docker"})  // Swagger attivo solo in ambiente local/dev/docker, disabilitato in prod
public class OpenApiConfig {

    @Bean
    public OpenAPI medsafeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedSafe API")
                        .description("API per la gestione sicura di referti medici su Azure")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MedSafe Team")
                                .email("support@medsafe.local"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
