package it.unisa.project.medsafe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

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

        @Value("${MEDSAFE_ENTRA_CLIENT_ID}")
        private String backendClientId;

        // Hardcoded per sicurezza: questo è l'ID del Frontend che sta chiamando
        // esplicitamente
        private static final String FRONTEND_CLIENT_ID = "5c911c10-3fe4-4569-b466-e79f78cd436f";

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                log.info("🔒 Configurazione Security: Abilitazione JWT con Audience Custom (Frontend ID)");

                http
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                customJwtAuthenticationConverter)
                                                                .decoder(jwtDecoder()))); // Usa il decoder custom

                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                // Usa l'endpoint COMMON per le chiavi pubbliche (multi-tenant)
                String jwkSetUri = "https://login.microsoftonline.com/common/discovery/v2.0/keys";

                NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

                // Validatore Custom per l'Audience
                OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(
                                List.of(backendClientId, FRONTEND_CLIENT_ID));
                // Validatore Default per l'Issuer

                jwtDecoder.setJwtValidator(audienceValidator);

                return jwtDecoder;
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

        /**
         * Validatore interno per l'Audience.
         * Accetta il token se l'audience match backend ID OPPURE frontend ID.
         */
        static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
                private final List<String> allowedAudiences;

                AudienceValidator(List<String> allowedAudiences) {
                        this.allowedAudiences = allowedAudiences;
                }

                public OAuth2TokenValidatorResult validate(Jwt jwt) {
                        List<String> audiences = jwt.getAudience();
                        if (audiences.stream().anyMatch(allowedAudiences::contains)) {
                                return OAuth2TokenValidatorResult.success();
                        }
                        log.warn("❌ Audience invalida nel token JWT");
                        return OAuth2TokenValidatorResult.failure(
                                        new OAuth2Error("invalid_token", "The required audience is missing", null));
                }
        }
}
