package it.unisa.project.medsafe.entity;

/**
 * Enumerazione per il genere dell'utente
 */
public enum Genere {
    MASCHIO("M", "Maschio"),
    FEMMINA("F", "Femmina"),
    NON_SPECIFICATO("N", "Non specificato");

    private final String codice;
    private final String descrizione;

    Genere(String codice, String descrizione) {
        this.codice = codice;
        this.descrizione = descrizione;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Trova Genere dal codice
     */
    public static Genere fromCodice(String codice) {
        for (Genere genere : values()) {
            if (genere.codice.equalsIgnoreCase(codice)) {
                return genere;
            }
        }
        return NON_SPECIFICATO;
    }
}
