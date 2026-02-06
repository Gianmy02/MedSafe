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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configurazione Security per Microsoft Entra ID (Azure AD).
 * Valida JWT tokens generati da Azure AD.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
@Profile({ "azure", "prod" }) // Attivo solo in produzione con Azure
public class SecurityConfig {

        @Value("${spring.cloud.azure.active-directory.credential.client-id}")
        private String clientId;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                log.info("🔐 Configurazione Security con Microsoft Entra ID");

                http
                                // Disabilita sessioni (Stateless REST API)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configurazione CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Disabilita CSRF (non necessario per API stateless)
                                .csrf(csrf -> csrf.disable())

                                // Configurazione autorizzazioni
                                .authorizeHttpRequests(auth -> auth
                                                // Endpoint pubblici (Swagger, Health check)
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/actuator/health")
                                                .permitAll()

                                                // Endpoint eliminazione: solo ADMIN
                                                .requestMatchers(HttpMethod.DELETE, "/referti/**").hasRole("ADMIN")

                                                // Tutti gli altri endpoint richiedono autenticazione
                                                .anyRequest().authenticated())

                                // Configurazione OAuth2 Resource Server con JWT
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter()))
                                                // Allow anonymous access: non bloccare richieste senza token
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // Se è un endpoint pubblico Swagger, permetti accesso anonimo
                                                        String requestPath = request.getRequestURI();
                                                        if (requestPath.startsWith("/swagger-ui") ||
                                                                        requestPath.startsWith("/v3/api-docs")) {
                                                                response.setStatus(200); // OK, procedi senza
                                                                                         // autenticazione
                                                                return;
                                                        }
                                                        // Altrimenti, ritorna 401 standard
                                                        response.sendError(401, "Unauthorized");
                                                }));

                return http.build();
        }

        /**
         * Configurazione CORS per permettere richieste dal frontend Angular.
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(
                                "https://*.azurestaticapps.net", // Azure Static Web Apps (pattern)
                                "https://*.azurewebsites.net", // Azure Web Apps (per Swagger)
                                "http://localhost:*" // Sviluppo locale (qualsiasi porta)
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Converte i claim JWT in authorities Spring Security.
         * Estrae i ruoli dal claim "roles" di Azure AD.
         */
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

                // Azure AD invia i ruoli nel claim "roles"
                grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

                // Aggiunge il prefisso "ROLE_" per compatibilità con Spring Security
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

                return jwtAuthenticationConverter;
        }
}
