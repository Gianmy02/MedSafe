package it.unisa.project.medsafe.entity;

import it.unisa.project.medsafe.entity.Specializzazione;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Specializzazione Enum")
class SpecializzazioneTest {

    @Test
    @DisplayName("Verifica numero totale specializzazioni")
    void testNumeroTotale() {
        Specializzazione[] specializzazioni = Specializzazione.values();
        assertEquals(42, specializzazioni.length, "Devono esserci 42 specializzazioni (inclusa NESSUNA)");
    }

    @Test
    @DisplayName("NESSUNA è la prima specializzazione")
    void testNessunaÈLaPrima() {
        Specializzazione[] specializzazioni = Specializzazione.values();
        assertEquals(Specializzazione.NESSUNA, specializzazioni[0],
                "NESSUNA deve essere la prima specializzazione");
    }

    @Test
    @DisplayName("Verifica descrizione NESSUNA")
    void testDescrizioneNessuna() {
        assertEquals("Nessuna", Specializzazione.NESSUNA.getDescrizione());
    }

    @Test
    @DisplayName("Verifica che NESSUNA sia la prima e le altre siano presenti")
    void testOrdinamentoSpecializzazioni() {
        Specializzazione[] specializzazioni = Specializzazione.values();

        // Verifica che NESSUNA sia prima
        assertEquals(Specializzazione.NESSUNA, specializzazioni[0]);

        // Verifica che ci siano almeno 40 specializzazioni oltre a NESSUNA
        assertTrue(specializzazioni.length >= 40,
                "Devono esserci almeno 40 specializzazioni oltre a NESSUNA");

        // Verifica che nessuna descrizione sia vuota
        for (Specializzazione spec : specializzazioni) {
            assertNotNull(spec.getDescrizione());
            assertFalse(spec.getDescrizione().isEmpty());
        }
    }

    @Test
    @DisplayName("Verifica alcune specializzazioni chiave esistono")
    void testSpecializzazioniChiaveEsistono() {
        assertNotNull(Specializzazione.CARDIOLOGIA);
        assertNotNull(Specializzazione.PEDIATRIA);
        assertNotNull(Specializzazione.NEUROLOGIA);
        assertNotNull(Specializzazione.ONCOLOGIA);
        assertNotNull(Specializzazione.MEDICINA_GENERALE);
        assertNotNull(Specializzazione.CHIRURGIA_GENERALE);
        assertNotNull(Specializzazione.ANESTESIA);
    }

    @Test
    @DisplayName("Verifica descrizioni di alcune specializzazioni")
    void testDescrizioniSpecifiche() {
        assertEquals("Cardiologia", Specializzazione.CARDIOLOGIA.getDescrizione());
        assertEquals("Pediatria", Specializzazione.PEDIATRIA.getDescrizione());
        assertEquals("Medicina Generale", Specializzazione.MEDICINA_GENERALE.getDescrizione());
        assertEquals("Anestesia e Rianimazione", Specializzazione.ANESTESIA.getDescrizione());
        assertEquals("Chirurgia Generale", Specializzazione.CHIRURGIA_GENERALE.getDescrizione());
    }

    @Test
    @DisplayName("fromDescrizione trova specializzazione corretta")
    void testFromDescrizione() {
        assertEquals(Specializzazione.CARDIOLOGIA,
                Specializzazione.fromDescrizione("Cardiologia"));
        assertEquals(Specializzazione.PEDIATRIA,
                Specializzazione.fromDescrizione("Pediatria"));
        assertEquals(Specializzazione.NESSUNA,
                Specializzazione.fromDescrizione("Nessuna"));
    }

    @Test
    @DisplayName("fromDescrizione è case-insensitive")
    void testFromDescrizioneCaseInsensitive() {
        assertEquals(Specializzazione.CARDIOLOGIA,
                Specializzazione.fromDescrizione("CARDIOLOGIA"));
        assertEquals(Specializzazione.PEDIATRIA,
                Specializzazione.fromDescrizione("pediatria"));
        assertEquals(Specializzazione.MEDICINA_GENERALE,
                Specializzazione.fromDescrizione("mEdIcInA gEnErAlE"));
    }

    @Test
    @DisplayName("fromDescrizione ritorna NESSUNA per descrizione invalida")
    void testFromDescrizioneInvalida() {
        assertEquals(Specializzazione.NESSUNA,
                Specializzazione.fromDescrizione("Specializzazione Inesistente"));
        assertEquals(Specializzazione.NESSUNA,
                Specializzazione.fromDescrizione(""));
        assertEquals(Specializzazione.NESSUNA,
                Specializzazione.fromDescrizione(null));
    }

    @Test
    @DisplayName("Verifica nuove specializzazioni aggiunte")
    void testNuoveSpecializzazioni() {
        assertNotNull(Specializzazione.ALLERGOLOGIA);
        assertNotNull(Specializzazione.ANATOMIA_PATOLOGICA);
        assertNotNull(Specializzazione.CARDIOCHIRURGIA);
        assertNotNull(Specializzazione.CHIRURGIA_PLASTICA);
        assertNotNull(Specializzazione.EMATOLOGIA);
        assertNotNull(Specializzazione.GENETICA_MEDICA);
        assertNotNull(Specializzazione.MALATTIE_INFETTIVE);
        assertNotNull(Specializzazione.NEUROCHIRURGIA);
        assertNotNull(Specializzazione.REUMATOLOGIA);
    }

    @Test
    @DisplayName("Verifica che tutte le specializzazioni abbiano descrizione non vuota")
    void testDescrizioniNonVuote() {
        for (Specializzazione spec : Specializzazione.values()) {
            assertNotNull(spec.getDescrizione(),
                    "La descrizione non deve essere null per " + spec.name());
            assertFalse(spec.getDescrizione().isEmpty(),
                    "La descrizione non deve essere vuota per " + spec.name());
        }
    }

    @Test
    @DisplayName("valueOf funziona correttamente")
    void testValueOf() {
        assertEquals(Specializzazione.CARDIOLOGIA,
                Specializzazione.valueOf("CARDIOLOGIA"));
        assertEquals(Specializzazione.PEDIATRIA,
                Specializzazione.valueOf("PEDIATRIA"));
        assertEquals(Specializzazione.NESSUNA,
                Specializzazione.valueOf("NESSUNA"));
    }

    @Test
    @DisplayName("valueOf lancia eccezione per valore inesistente")
    void testValueOfInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> Specializzazione.valueOf("SPECIALIZZAZIONE_INESISTENTE"));
        assertThrows(IllegalArgumentException.class,
                () -> Specializzazione.valueOf(""));
    }

    @Test
    @DisplayName("Verifica specializzazioni chirurgiche")
    void testSpecializzazioniChirurgiche() {
        assertTrue(Specializzazione.CHIRURGIA_GENERALE.getDescrizione().contains("Chirurgia"));
        assertTrue(Specializzazione.CHIRURGIA_PLASTICA.getDescrizione().contains("Chirurgia"));
        assertTrue(Specializzazione.CHIRURGIA_TORACICA.getDescrizione().contains("Chirurgia"));
        assertTrue(Specializzazione.CHIRURGIA_VASCOLARE.getDescrizione().contains("Chirurgia"));
        assertTrue(Specializzazione.CARDIOCHIRURGIA.getDescrizione().contains("chirurgia"));
        assertTrue(Specializzazione.NEUROCHIRURGIA.getDescrizione().contains("chirurgia"));
    }

    @Test
    @DisplayName("Verifica specializzazioni di medicina")
    void testSpecializzazioniMedicina() {
        assertNotNull(Specializzazione.MEDICINA_GENERALE);
        assertNotNull(Specializzazione.MEDICINA_INTERNA);
        assertNotNull(Specializzazione.MEDICINA_LEGALE);
        assertNotNull(Specializzazione.MEDICINA_FISICA);
        assertNotNull(Specializzazione.MEDICINA_EMERGENZA);
    }
}
