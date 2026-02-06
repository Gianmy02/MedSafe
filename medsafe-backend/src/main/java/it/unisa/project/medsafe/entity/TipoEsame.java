package it.unisa.project.medsafe.entity;

public enum TipoEsame {
    TAC("TAC"),
    Radiografia("Radiografia"),
    Ecografia("Ecografia"),
    Risonanza("Risonanza"),
    Esami_Laboratorio("Esami di Laboratorio");

    private final String descrizione;

    TipoEsame(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
