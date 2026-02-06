package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Crea o aggiorna un utente basato sui dati del JWT di Azure AD.
     *
     * @param email    Email dell'utente
     * @param fullName Nome completo
     * @param azureOid Object ID di Azure AD
     * @param role     Ruolo dell'utente
     * @return User creato o aggiornato
     */
    User syncUserFromAzureAd(String email, String fullName, String azureOid, UserRole role);

    /**
     * Trova un utente tramite email.
     *
     * @param email Email dell'utente
     * @return Optional contenente l'utente se trovato
     */
    Optional<User> findByEmail(String email);

    /**
     * Trova un utente tramite Azure OID.
     *
     * @param azureOid Object ID di Azure AD
     * @return Optional contenente l'utente se trovato
     */
    Optional<User> findByAzureOid(String azureOid);

    /**
     * Ottiene tutti gli utenti.
     *
     * @return Lista di tutti gli utenti
     */
    List<User> getAllUsers();

    /**
     * Aggiorna il profilo utente (genere e specializzazione).
     *
     * @param email Email dell'utente
     * @param genere Nuovo genere
     * @param specializzazione Nuova specializzazione
     * @return User aggiornato
     */
    Optional<User> updateUserProfile(String email, Genere genere, Specializzazione specializzazione);

    /**
     * Disabilita un utente.
     *
     * @param id ID dell'utente
     * @return true se l'operazione è andata a buon fine
     */
    boolean disableUser(int id);

    /**
     * Abilita un utente.
     *
     * @param id ID dell'utente
     * @return true se l'operazione è andata a buon fine
     */
    boolean enableUser(int id);
}
