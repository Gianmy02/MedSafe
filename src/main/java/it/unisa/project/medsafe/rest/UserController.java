package it.unisa.project.medsafe.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unisa.project.medsafe.dto.UserDTO;
import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.service.UserService;
import it.unisa.project.medsafe.utils.JwtHelper;
import it.unisa.project.medsafe.utils.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Utenti", description = "API per la gestione degli utenti")
public class UserController {

    private final UserService userService;
    private final JwtHelper jwtHelper;
    private final UserMapper userMapper;

    @Operation(summary = "Ottieni info utente corrente", description = "Restituisce le informazioni dell'utente autenticato")
    @GetMapping("me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        String email = jwtHelper.getCurrentUserEmail();

        // In modalità local/docker senza JWT, usa utente di fallback
        if (email == null) {
            log.warn("⚠️  Nessun JWT trovato, usando utente di fallback per testing locale");
            email = "admin@medsafe.local";
        }

        log.info("📋 Richiesta info utente: {}", email);

        // Recupera dati completi dal database
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            log.warn("⚠️  Utente non trovato: {}", email);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userMapper.userToUserDTO(user));
    }

    @Operation(summary = "Aggiorna profilo utente", description = "Permette a qualsiasi utente di modificare il proprio genere e specializzazione")
    @PutMapping("profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UserDTO userDTO) {

        String email = jwtHelper.getCurrentUserEmail();
        // In modalità local/docker senza JWT, usa utente di fallback
        if (email == null) {
            log.warn("⚠️  Nessun JWT trovato, usando utente di fallback per testing locale");
            email = "admin@medsafe.local";
        }
        log.info("📝 Aggiornamento profilo per: {}", email);

        var updatedUser = userService.updateUserProfile(email, userDTO.getGenere(), userDTO.getSpecializzazione());

        if (updatedUser.isPresent()) {
            log.info("✅ Profilo aggiornato con successo");
            return ResponseEntity.ok(userMapper.userToUserDTO(updatedUser.get()));
        } else {
            log.error("❌ Utente non trovato: {}", userDTO.getEmail());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Ottieni tutti gli utenti", description = "Lista di tutti gli utenti (solo Admin in produzione)")
    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')") // ← Commentato per testing in modalità local/docker
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("📋 Richiesta lista utenti");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.usersToUsersDTO(users));
    }

    @Operation(summary = "Disabilita utente", description = "Disabilita un utente (solo Admin)")
    @PutMapping("{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable int id) {
        log.info("🚫 Disabilitazione utente ID: {}", id);
        if (userService.disableUser(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Abilita utente", description = "Abilita un utente (solo Admin)")
    @PutMapping("{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable int id) {
        log.info("✅ Abilitazione utente ID: {}", id);
        if (userService.enableUser(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Ottieni tutti i generi", description = "Restituisce l'elenco di tutti i generi disponibili")
    @GetMapping("generi")
    public ResponseEntity<Genere[]> getAllGeneri() {
        log.info("📋 Richiesta lista generi");
        return ResponseEntity.ok(Genere.values());
    }

    @Operation(summary = "Ottieni tutte le specializzazioni", description = "Restituisce l'elenco di tutte le specializzazioni mediche disponibili")
    @GetMapping("specializzazioni")
    public ResponseEntity<Specializzazione[]> getAllSpecializzazioni() {
        log.info("📋 Richiesta lista specializzazioni");
        return ResponseEntity.ok(Specializzazione.values());
    }
}
