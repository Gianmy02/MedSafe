package it.unisa.project.medsafe.entinty;

public enum TipoEsame {
    TAC("TAC"),
    RADIOGRAFIA("Radiografia"),
    ECOGRAFIA("Ecografia"),
    RISONANZA("Risonanza"),
    ESAMI_LABORATORIO("Esami di Laboratorio");

    private final String descrizione;

    TipoEsame(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
