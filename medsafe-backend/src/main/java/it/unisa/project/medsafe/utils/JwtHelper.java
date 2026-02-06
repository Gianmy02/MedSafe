package it.unisa.project.medsafe.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Utility per estrarre informazioni dal JWT di Azure AD.
 */
@Component
@Slf4j
public class JwtHelper {

    /**
     * Ottiene l'email dell'utente autenticato dal JWT.
     *
     * @return Email dell'utente o null se non autenticato
     */
    public String getCurrentUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                // Azure AD può usare "email" o "preferred_username"
                String email = jwt.getClaim("email");
                if (email == null || email.isEmpty()) {
                    email = jwt.getClaim("preferred_username");
                }
                return email;
            }
        } catch (Exception e) {
            log.warn("⚠️  Impossibile estrarre email dal JWT: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Ottiene il nome completo dell'utente dal JWT.
     *
     * @return Nome completo o null se non presente
     */
    public String getCurrentUserFullName() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                return jwt.getClaim("name");
            }
        } catch (Exception e) {
            log.warn("⚠️  Impossibile estrarre nome dal JWT: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Ottiene l'Object ID di Azure AD dal JWT.
     *
     * @return Azure OID o null se non presente
     */
    public String getCurrentUserAzureOid() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                return jwt.getClaim("oid");
            }
        } catch (Exception e) {
            log.warn("⚠️  Impossibile estrarre OID dal JWT: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Verifica se l'utente corrente ha un determinato ruolo.
     *
     * @param role Nome del ruolo (es: "MEDICO", "ADMIN")
     * @return true se l'utente ha il ruolo
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Ottiene il JWT completo.
     *
     * @return JWT token o null
     */
    public Jwt getCurrentJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                return jwtAuth.getToken();
            }
        } catch (Exception e) {
            log.warn("⚠️  Impossibile estrarre JWT: {}", e.getMessage());
        }
        return null;
    }
}
