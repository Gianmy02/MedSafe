package it.unisa.project.medsafe.config;

import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Custom JWT Authentication Converter che:
 * 1. Estrae i dati dell'utente dal JWT di Azure AD
 * 2. Carica i ruoli dal database (se l'utente esiste)
 * 3. Usa ruolo MEDICO di default se l'utente non esiste ancora nel DB
 *
 * NOTA: La sincronizzazione dell'utente nel DB avviene nell'endpoint /users/me
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Estrai dati dal JWT di Azure AD
        String email = extractEmail(jwt);

        if (email == null || email.isEmpty()) {
            log.error("❌ JWT senza email valida!");
            return new JwtAuthenticationToken(jwt, Collections.emptyList());
        }

        log.debug("🔐 Autenticazione JWT per: {}", email);

        // 2. Carica i ruoli dal database (se l'utente esiste)
        Collection<GrantedAuthority> authorities = loadUserAuthorities(email);

        // 3. Crea il token di autenticazione
        return new JwtAuthenticationToken(jwt, authorities, email);
    }

    /**
     * Estrae l'email dal JWT (Azure AD può usare "email" o "preferred_username").
     */
    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaim("email");
        if (email == null || email.isEmpty()) {
            email = jwt.getClaim("preferred_username");
        }
        return email;
    }

    /**
     * Carica i ruoli dell'utente dal database.
     * Se l'utente non esiste ancora, assegna ruolo MEDICO di default (verrà creato al primo accesso).
     */
    private Collection<GrantedAuthority> loadUserAuthorities(String email) {
        Optional<User> user = userService.findByEmail(email);

        if (user.isPresent() && user.get().getRole() != null) {
            // Utente esiste nel DB: usa il suo ruolo
            String role = "ROLE_" + user.get().getRole().name();
            log.debug("🔑 Ruolo caricato dal DB per {}: {}", email, role);
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        } else {
            // Utente NON esiste ancora nel DB: assegna ruolo MEDICO temporaneo
            // (l'utente verrà creato nel DB al primo accesso a /users/me)
            log.debug("👤 Utente {} non trovato nel DB, assegnato ruolo MEDICO temporaneo", email);
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEDICO"));
        }
    }
}

