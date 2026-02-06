package it.unisa.project.medsafe.entity;

/**
 * Enumerazione per la specializzazione medica
 */
public enum Specializzazione {
    // Nessuna specializzazione (prima voce)
    NESSUNA("Nessuna"), // Per utenti Admin o non medici

    // Specializzazioni in ordine alfabetico
    ALLERGOLOGIA("Allergologia e Immunologia Clinica"),
    ANATOMIA_PATOLOGICA("Anatomia Patologica"),
    ANDROLOGIA("Andrologia"),
    ANESTESIA("Anestesia e Rianimazione"),
    ANGIOLOGIA("Angiologia"),
    CARDIOCHIRURGIA("Cardiochirurgia"),
    CARDIOLOGIA("Cardiologia"),
    CHIRURGIA_GENERALE("Chirurgia Generale"),
    CHIRURGIA_PLASTICA("Chirurgia Plastica e Ricostruttiva"),
    CHIRURGIA_TORACICA("Chirurgia Toracica"),
    CHIRURGIA_VASCOLARE("Chirurgia Vascolare"),
    DERMATOLOGIA("Dermatologia"),
    EMATOLOGIA("Ematologia"),
    ENDOCRINOLOGIA("Endocrinologia"),
    EPATOLOGIA("Epatologia"),
    GASTROENTEROLOGIA("Gastroenterologia"),
    GENETICA_MEDICA("Genetica Medica"),
    GERIATRIA("Geriatria"),
    GINECOLOGIA("Ginecologia"),
    IGIENE("Igiene e Medicina Preventiva"),
    MALATTIE_INFETTIVE("Malattie Infettive"),
    MEDICINA_EMERGENZA("Medicina d'Emergenza-Urgenza"),
    MEDICINA_FISICA("Medicina Fisica e Riabilitativa"),
    MEDICINA_GENERALE("Medicina Generale"),
    MEDICINA_INTERNA("Medicina Interna"),
    MEDICINA_LEGALE("Medicina Legale"),
    NEFROLOGIA("Nefrologia"),
    NEONATOLOGIA("Neonatologia"),
    NEUROCHIRURGIA("Neurochirurgia"),
    NEUROLOGIA("Neurologia"),
    OCULISTICA("Oculistica"),
    ONCOLOGIA("Oncologia"),
    ORTOPEDIA("Ortopedia"),
    OSTETRICIA("Ostetricia"),
    OTORINOLARINGOIATRIA("Otorinolaringoiatria"),
    PEDIATRIA("Pediatria"),
    PNEUMOLOGIA("Pneumologia"),
    PSICHIATRIA("Psichiatria"),
    RADIOLOGIA("Radiologia"),
    REUMATOLOGIA("Reumatologia"),
    UROLOGIA("Urologia");

    private final String descrizione;

    Specializzazione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Trova Specializzazione dalla descrizione
     */
    public static Specializzazione fromDescrizione(String descrizione) {
        for (Specializzazione spec : values()) {
            if (spec.descrizione.equalsIgnoreCase(descrizione)) {
                return spec;
            }
        }
        return NESSUNA;
    }
}

