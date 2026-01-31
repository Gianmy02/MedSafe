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
        assertNotNull(TipoEsame.RADIOGRAFIA);
        assertNotNull(TipoEsame.ECOGRAFIA);
        assertNotNull(TipoEsame.RISONANZA);
        assertNotNull(TipoEsame.ESAMI_LABORATORIO);
    }

    @Test
    @DisplayName("Verifica descrizioni dei tipi esame")
    void testDescrizioni() {
        assertEquals("TAC", TipoEsame.TAC.getDescrizione());
        assertEquals("Radiografia", TipoEsame.RADIOGRAFIA.getDescrizione());
        assertEquals("Ecografia", TipoEsame.ECOGRAFIA.getDescrizione());
        assertEquals("Risonanza", TipoEsame.RISONANZA.getDescrizione());
        assertEquals("Esami di Laboratorio", TipoEsame.ESAMI_LABORATORIO.getDescrizione());
    }

    @Test
    @DisplayName("Verifica valueOf")
    void testValueOf() {
        assertEquals(TipoEsame.TAC, TipoEsame.valueOf("TAC"));
        assertEquals(TipoEsame.RADIOGRAFIA, TipoEsame.valueOf("RADIOGRAFIA"));
        assertEquals(TipoEsame.ECOGRAFIA, TipoEsame.valueOf("ECOGRAFIA"));
        assertEquals(TipoEsame.RISONANZA, TipoEsame.valueOf("RISONANZA"));
        assertEquals(TipoEsame.ESAMI_LABORATORIO, TipoEsame.valueOf("ESAMI_LABORATORIO"));
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

        assertEquals("RADIOGRAFIA", TipoEsame.RADIOGRAFIA.name());
        assertEquals(1, TipoEsame.RADIOGRAFIA.ordinal());

        assertEquals("ECOGRAFIA", TipoEsame.ECOGRAFIA.name());
        assertEquals(2, TipoEsame.ECOGRAFIA.ordinal());

        assertEquals("RISONANZA", TipoEsame.RISONANZA.name());
        assertEquals(3, TipoEsame.RISONANZA.ordinal());

        assertEquals("ESAMI_LABORATORIO", TipoEsame.ESAMI_LABORATORIO.name());
        assertEquals(4, TipoEsame.ESAMI_LABORATORIO.ordinal());
    }
}
