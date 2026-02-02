package it.unisa.project.medsafe.entity;

import it.unisa.project.medsafe.BasePojoTest;
import it.unisa.project.medsafe.entinty.TipoEsame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Enum TipoEsame")
class TipoEsameTest extends BasePojoTest {

    @Test
    @DisplayName("Verifica tutti i valori dell'enum TipoEsame")
    void testAllEnumValues() {
        TipoEsame[] values = TipoEsame.values();

        assertEquals(5, values.length);
        assertNotNull(TipoEsame.TAC);
        assertNotNull(TipoEsame.Radiografia);
        assertNotNull(TipoEsame.Ecografia);
        assertNotNull(TipoEsame.Risonanza);
        assertNotNull(TipoEsame.Esami_Laboratorio);
    }

    @Test
    @DisplayName("Verifica descrizioni dei tipi esame")
    void testDescrizioni() {
        assertEquals("TAC", TipoEsame.TAC.getDescrizione());
        assertEquals("Radiografia", TipoEsame.Radiografia.getDescrizione());
        assertEquals("Ecografia", TipoEsame.Ecografia.getDescrizione());
        assertEquals("Risonanza", TipoEsame.Risonanza.getDescrizione());
        assertEquals("Esami di Laboratorio", TipoEsame.Esami_Laboratorio.getDescrizione());
    }

    @Test
    @DisplayName("Verifica valueOf")
    void testValueOf() {
        assertEquals(TipoEsame.TAC, TipoEsame.valueOf("TAC"));
        assertEquals(TipoEsame.Radiografia, TipoEsame.valueOf("Radiografia"));
        assertEquals(TipoEsame.Ecografia, TipoEsame.valueOf("Ecografia"));
        assertEquals(TipoEsame.Risonanza, TipoEsame.valueOf("Risonanza"));
        assertEquals(TipoEsame.Esami_Laboratorio, TipoEsame.valueOf("Esami_Laboratorio"));
    }

    @Test
    @DisplayName("Verifica valueOf con valore non valido")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TipoEsame.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Verifica name e ordinal")
    void testNameAndOrdinal() {
        assertEquals("TAC", TipoEsame.TAC.name());
        assertEquals(0, TipoEsame.TAC.ordinal());

        assertEquals("Radiografia", TipoEsame.Radiografia.name());
        assertEquals(1, TipoEsame.Radiografia.ordinal());

        assertEquals("Ecografia", TipoEsame.Ecografia.name());
        assertEquals(2, TipoEsame.Ecografia.ordinal());

        assertEquals("Risonanza", TipoEsame.Risonanza.name());
        assertEquals(3, TipoEsame.Risonanza.ordinal());

        assertEquals("Esami_Laboratorio", TipoEsame.Esami_Laboratorio.name());
        assertEquals(4, TipoEsame.Esami_Laboratorio.ordinal());
    }
}
