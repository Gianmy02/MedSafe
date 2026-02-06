package it.unisa.project.medsafe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione Security per ambiente di sviluppo locale/docker.
 * NON usa Azure AD, permette accesso libero per testing.
 */
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true) // ← Disabilitato per evitare problemi in dev
@Slf4j
@Profile({"local", "docker", "test"})
public class SecurityConfigLocal {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.warn("⚠️  Security DISABILITATA - Ambiente: LOCAL/DOCKER/TEST");
        log.warn("⚠️  NON usare questa configurazione in produzione!");

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Permetti tutto in sviluppo
                );

        return http.build();
    }
}
