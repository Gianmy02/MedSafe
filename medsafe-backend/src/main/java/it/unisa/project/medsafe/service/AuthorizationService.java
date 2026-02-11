package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.exception.UnauthorizedException;
import it.unisa.project.medsafe.repository.UserRepository;
import it.unisa.project.medsafe.utils.JwtHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service per gestire l'autorizzazione degli utenti sui referti.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final JwtHelper jwtHelper;
    private final UserRepository userRepository;

    /**
     * Verifica se l'utente corrente può modificare/eliminare il referto.
     *
     * Regole:
     * - ADMIN: può modificare/eliminare qualsiasi referto
     * - MEDICO: può modificare/eliminare solo i propri referti (stesso autoreEmail)
     *
     * @param referto   Il referto da verificare
     * @param operation Tipo di operazione ("modificare" o "eliminare") per il
     *                  messaggio
     * @throws UnauthorizedException se l'utente non ha i permessi
     */
    public void checkCanModifyReferto(Referto referto, String operation) {
        String currentUserEmail = getCurrentUserEmail();

        // Verifica se l'utente è abilitato
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("Utente non trovato"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Utente non abilitato. Contattare l'amministratore.");
        }

        // Verifica se è ADMIN
        if (isAdmin(currentUserEmail)) {
            log.info("✅ ADMIN {} può {} il referto ID {}", currentUserEmail, operation, referto.getId());
            return;
        }

        // Verifica se è il proprietario
        if (referto.getAutoreEmail().equalsIgnoreCase(currentUserEmail)) {
            log.info("✅ MEDICO {} può {} il proprio referto ID {}", currentUserEmail, operation, referto.getId());
            return;
        }

        // Non autorizzato
        log.warn("🚫 MEDICO {} NON può {} il referto ID {} (proprietario: {})",
                currentUserEmail, operation, referto.getId(), referto.getAutoreEmail());
        throw new UnauthorizedException(
                String.format(
                        "Non sei autorizzato a %s questo referto. Solo il medico che lo ha creato (%s) o un amministratore può farlo.",
                        operation, referto.getAutoreEmail()));
    }

    /**
     * Verifica se l'utente corrente può aggiungere un referto.
     */
    public void checkCanAddReferto() {
        String currentUserEmail = getCurrentUserEmail();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UnauthorizedException("Utente non trovato"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Utente non abilitato. Contattare l'amministratore.");
        }
    }

    /**
     * Ottiene l'email dell'utente corrente (da JWT o fallback).
     */
    private String getCurrentUserEmail() {
        String email = jwtHelper.getCurrentUserEmail();
        return email;
    }

    /**
     * Verifica se l'utente ha ruolo ADMIN.
     */
    private boolean isAdmin(String email) {
        // In modalità docker/local, controlla nel database
        /*
         * Optional<User> user = userRepository.findByEmail(email);
         * if (user.isPresent() && user.get().getRole() == UserRole.ADMIN) {
         * return true;
         * }
         */

        // In modalità Azure, verifica i ruoli dal JWT
        return jwtHelper.hasRole("ADMIN");
    }

    /**
     * Ottiene l'email dell'utente corrente (pubblico per uso nei controller).
     */
    public String getCurrentUserEmailPublic() {
        return getCurrentUserEmail();
    }

    /**
     * Verifica se l'utente corrente è un ADMIN (pubblico per uso nei controller).
     */
    public boolean isCurrentUserAdmin() {
        return isAdmin(getCurrentUserEmail());
    }
}
