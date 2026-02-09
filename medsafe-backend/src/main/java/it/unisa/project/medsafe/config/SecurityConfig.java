package it.unisa.project.medsafe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configurazione Security SENZA autenticazione.
 * Il frontend è protetto da EasyAuth, il backend accetta tutte le richieste.
 * Protezione basata solo su CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
@Profile({ "azure", "prod" })
public class SecurityConfig {

        private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                log.info("🔒 Configurazione Security: Abilitazione JWT Oauth2 Resource Server");

                http
                                // Disabilita sessioni (Stateless REST API)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configurazione CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Disabilita CSRF
                                .csrf(csrf -> csrf.disable())

                                // Configura Resource Server
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                customJwtAuthenticationConverter)))

                                // Regole di autorizzazione
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(
                                "https://*.azurestaticapps.net",
                                "https://*.azurewebsites.net",
                                "http://localhost:*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
