package it.unisa.project.medsafe.dto;

import it.unisa.project.medsafe.entity.TipoEsame;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO per la creazione/modifica di un referto.
 * Usato per ricevere dati dal frontend e validarli.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefertoDTO {

    private int id;

    @NotBlank(message = "Il nome del paziente è obbligatorio")
    private String nomePaziente;

    @NotBlank(message = "Il codice fiscale è obbligatorio")
    @Size(min = 16, max = 16, message = "Il codice fiscale deve essere di 16 caratteri")
    @Pattern(regexp = "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$",
             message = "Formato codice fiscale non valido")
    private String codiceFiscale;

    @NotNull(message = "Il tipo di esame è obbligatorio")
    private TipoEsame tipoEsame;

    @NotBlank(message = "Il testo del referto è obbligatorio")
    @Size(max = 4000, message = "Il testo del referto non può superare 4000 caratteri")
    private String testoReferto;

    @NotBlank(message = "Le conclusioni sono obbligatorie")
    @Size(max = 4000, message = "Le conclusioni non possono superare 4000 caratteri")
    private String conclusioni;

    @NotBlank(message = "L'URL dell'immagine è obbligatorio")
    private String fileUrlImmagine;

    private String urlPdfGenerato;

    @NotBlank(message = "Il nome del file è obbligatorio")
    private String nomeFile;

    @NotBlank(message = "L'email dell'autore è obbligatoria")
    @Email(message = "Formato email non valido")
    private String autoreEmail;

    private LocalDateTime dataCaricamento;
}
