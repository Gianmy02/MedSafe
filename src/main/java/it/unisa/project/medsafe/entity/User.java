package it.unisa.project.medsafe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity User per gestione utenti autenticati con Microsoft Entra ID.
 * Non contiene password (gestite da Azure AD).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Email univoca dell'utente (proveniente da Azure AD).
     * Usata come username per l'autenticazione.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Object ID da Azure AD (identificatore univoco del tenant).
     * Opzionale ma utile per tracciare l'utente su Azure.
     */
    @Column(length = 100)
    private String azureOid;

    /**
     * Nome completo dell'utente.
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * Genere dell'utente (opzionale).
     */
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Genere genere;

    /**
     * Specializzazione medica (opzionale, solo per medici).
     */
    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private Specializzazione specializzazione;

    /**
     * Ruolo dell'utente nel sistema.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * Indica se l'account è attivo.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Data di creazione dell'utente.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
