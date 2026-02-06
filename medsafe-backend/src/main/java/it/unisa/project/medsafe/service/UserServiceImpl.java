package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;
import it.unisa.project.medsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User syncUserFromAzureAd(String email, String fullName, String azureOid, UserRole role) {
        log.info("🔄 Sincronizzazione utente da Azure AD: {}", email);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Aggiorna utente esistente
            User user = existingUser.get();
            user.setFullName(fullName);
            user.setAzureOid(azureOid);
            user.setRole(role);
            log.info("✅ Utente aggiornato: {}", email);
            return userRepository.save(user);
        } else {
            // Crea nuovo utente
            User newUser = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .azureOid(azureOid)
                    .role(role)
                    .enabled(true)
                    .build();
            log.info("✅ Nuovo utente creato: {}", email);
            return userRepository.save(newUser);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByAzureOid(String azureOid) {
        return userRepository.findByAzureOid(azureOid);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> updateUserProfile(String email, Genere genere, Specializzazione specializzazione) {
        log.info("📝 Aggiornamento profilo utente: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setGenere(genere);
            user.setSpecializzazione(specializzazione);
            User updated = userRepository.save(user);
            log.info("✅ Profilo aggiornato: {} - Genere: {}, Specializzazione: {}",
                    email, genere, specializzazione);
            return Optional.of(updated);
        }

        log.warn("⚠️  Utente non trovato: {}", email);
        return Optional.empty();
    }

    @Override
    public boolean disableUser(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setEnabled(false);
            userRepository.save(user.get());
            log.info("🚫 Utente disabilitato: ID {}", id);
            return true;
        }
        return false;
    }

    @Override
    public boolean enableUser(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setEnabled(true);
            userRepository.save(user.get());
            log.info("✅ Utente abilitato: ID {}", id);
            return true;
        }
        return false;
    }
}
