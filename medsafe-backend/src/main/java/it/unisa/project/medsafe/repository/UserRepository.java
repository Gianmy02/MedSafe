package it.unisa.project.medsafe.repository;

import it.unisa.project.medsafe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Trova un utente tramite email.
     *
     * @param email Email dell'utente
     * @return Optional contenente l'utente se trovato
     */
    Optional<User> findByEmail(String email);

    /**
     * Trova un utente tramite Azure Object ID.
     *
     * @param azureOid Object ID da Azure AD
     * @return Optional contenente l'utente se trovato
     */
    Optional<User> findByAzureOid(String azureOid);

    /**
     * Verifica se esiste un utente con questa email.
     *
     * @param email Email da verificare
     * @return true se esiste
     */
    boolean existsByEmail(String email);
}
