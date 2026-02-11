package it.unisa.project.medsafe.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Genere Enum")
class GenereTest {

    @Test
    @DisplayName("Verifica tutti i valori di Genere")
    void testTuttiIValori() {
        Genere[] generi = Genere.values();

        assertEquals(3, generi.length, "Devono esserci esattamente 3 valori");
        assertEquals(Genere.MASCHIO, generi[0]);
        assertEquals(Genere.FEMMINA, generi[1]);
        assertEquals(Genere.NON_SPECIFICATO, generi[2]);
    }

    @Test
    @DisplayName("Verifica codici genere")
    void testCodici() {
        assertEquals("M", Genere.MASCHIO.getCodice());
        assertEquals("F", Genere.FEMMINA.getCodice());
        assertEquals("N", Genere.NON_SPECIFICATO.getCodice());
    }

    @Test
    @DisplayName("Verifica descrizioni genere")
    void testDescrizioni() {
        assertEquals("Maschio", Genere.MASCHIO.getDescrizione());
        assertEquals("Femmina", Genere.FEMMINA.getDescrizione());
        assertEquals("Non specificato", Genere.NON_SPECIFICATO.getDescrizione());
    }

    @Test
    @DisplayName("fromCodice ritorna genere corretto")
    void testFromCodice() {
        assertEquals(Genere.MASCHIO, Genere.fromCodice("M"));
        assertEquals(Genere.FEMMINA, Genere.fromCodice("F"));
        assertEquals(Genere.NON_SPECIFICATO, Genere.fromCodice("N"));
    }

    @Test
    @DisplayName("fromCodice è case-insensitive")
    void testFromCodiceCaseInsensitive() {
        assertEquals(Genere.MASCHIO, Genere.fromCodice("m"));
        assertEquals(Genere.FEMMINA, Genere.fromCodice("f"));
        assertEquals(Genere.NON_SPECIFICATO, Genere.fromCodice("n"));
    }

    @Test
    @DisplayName("fromCodice ritorna NON_SPECIFICATO per codice invalido")
    void testFromCodiceInvalido() {
        assertEquals(Genere.NON_SPECIFICATO, Genere.fromCodice("X"));
        assertEquals(Genere.NON_SPECIFICATO, Genere.fromCodice(""));
        assertEquals(Genere.NON_SPECIFICATO, Genere.fromCodice(null));
    }

    @Test
    @DisplayName("Verifica che ALTRO non esista più")
    void testAltroNonEsiste() {
        Genere[] generi = Genere.values();
        for (Genere genere : generi) {
            assertNotEquals("ALTRO", genere.name(), "ALTRO non deve esistere");
        }
    }

    @Test
    @DisplayName("valueOf funziona correttamente")
    void testValueOf() {
        assertEquals(Genere.MASCHIO, Genere.valueOf("MASCHIO"));
        assertEquals(Genere.FEMMINA, Genere.valueOf("FEMMINA"));
        assertEquals(Genere.NON_SPECIFICATO, Genere.valueOf("NON_SPECIFICATO"));
    }

    @Test
    @DisplayName("valueOf lancia eccezione per valore inesistente")
    void testValueOfInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Genere.valueOf("ALTRO"));
        assertThrows(IllegalArgumentException.class, () -> Genere.valueOf("UNKNOWN"));
    }
}
