package it.unisa.project.medsafe.dto;

import it.unisa.project.medsafe.entity.Genere;
import it.unisa.project.medsafe.entity.Specializzazione;
import it.unisa.project.medsafe.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO per la gestione degli utenti.
 * Usato per ricevere/inviare dati dal/al frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private int id;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    private String email;

    @NotBlank(message = "Il nome completo è obbligatorio")
    private String fullName;

    private String azureOid;

    private Genere genere;

    private Specializzazione specializzazione;

    private UserRole role;

    private boolean enabled;

    private LocalDateTime createdAt;
}
