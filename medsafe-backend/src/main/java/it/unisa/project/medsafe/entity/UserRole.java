package it.unisa.project.medsafe.entity;

/**
 * Enum per i ruoli degli utenti nel sistema MedSafe.
 */
public enum UserRole {
    /**
     * Medico: può creare, modificare e visualizzare referti.
     */
    MEDICO("Medico"),

    /**
     * Amministratore: ha tutti i permessi, inclusa l'eliminazione.
     */
    ADMIN("Amministratore");

    private final String descrizione;

    UserRole(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
