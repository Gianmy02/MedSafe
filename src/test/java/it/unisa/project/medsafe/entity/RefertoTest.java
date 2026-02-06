package it.unisa.project.medsafe.entity;

import it.unisa.project.medsafe.BasePojoTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test POJO Referto Entity")
class RefertoTest extends BasePojoTest {

    @Nested
    @DisplayName("Test Getter e Setter")
    class GetterSetterTests {

        @Test
        @DisplayName("Test getter e setter per tutti i campi")
        void testGettersAndSetters() {
            Referto referto = new Referto();
            LocalDateTime now = LocalDateTime.now();

            referto.setId(1);
            referto.setNomePaziente("Mario Rossi");
            referto.setCodiceFiscale("RSSMRA80A01H501Z");
            referto.setTipoEsame(TipoEsame.TAC);
            referto.setTestoReferto("Testo del referto");
            referto.setConclusioni("Conclusioni");
            referto.setFileUrlImmagine("http://test/img.png");
            referto.setUrlPdfGenerato("http://test/pdf.pdf");
            referto.setNomeFile("referto_rossi");
            referto.setAutoreEmail("dott@hospital.com");
            referto.setDataCaricamento(now);

            assertEquals(1, referto.getId());
            assertEquals("Mario Rossi", referto.getNomePaziente());
            assertEquals("RSSMRA80A01H501Z", referto.getCodiceFiscale());
            assertEquals(TipoEsame.TAC, referto.getTipoEsame());
            assertEquals("Testo del referto", referto.getTestoReferto());
            assertEquals("Conclusioni", referto.getConclusioni());
            assertEquals("http://test/img.png", referto.getFileUrlImmagine());
            assertEquals("http://test/pdf.pdf", referto.getUrlPdfGenerato());
            assertEquals("referto_rossi", referto.getNomeFile());
            assertEquals("dott@hospital.com", referto.getAutoreEmail());
            assertEquals(now, referto.getDataCaricamento());
        }
    }

    @Nested
    @DisplayName("Test Builder")
    class BuilderTests {

        @Test
        @DisplayName("Crea Referto con builder - tutti i campi")
        void testBuilderComplete() {
            LocalDateTime now = LocalDateTime.now();

            Referto referto = Referto.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Testo del referto")
                    .conclusioni("Conclusioni del referto")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_rossi")
                    .autoreEmail("dott@hospital.com")
                    .dataCaricamento(now)
                    .build();

            assertEquals(1, referto.getId());
            assertEquals("Mario Rossi", referto.getNomePaziente());
            assertEquals("RSSMRA80A01H501Z", referto.getCodiceFiscale());
            assertEquals(TipoEsame.TAC, referto.getTipoEsame());
            assertEquals("Testo del referto", referto.getTestoReferto());
            assertEquals("Conclusioni del referto", referto.getConclusioni());
            assertEquals("http://test/img.png", referto.getFileUrlImmagine());
            assertEquals("http://test/pdf.pdf", referto.getUrlPdfGenerato());
            assertEquals("referto_rossi", referto.getNomeFile());
            assertEquals("dott@hospital.com", referto.getAutoreEmail());
            assertEquals(now, referto.getDataCaricamento());
        }

        @Test
        @DisplayName("Crea Referto con builder - campi minimi")
        void testBuilderMinimal() {
            Referto referto = Referto.builder()
                    .nomePaziente("Luigi Bianchi")
                    .codiceFiscale("BNCLGU85B02F205X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .build();

            assertEquals(0, referto.getId());
            assertEquals("Luigi Bianchi", referto.getNomePaziente());
            assertEquals("BNCLGU85B02F205X", referto.getCodiceFiscale());
            assertEquals(TipoEsame.Radiografia, referto.getTipoEsame());
            assertNull(referto.getTestoReferto());
            assertNull(referto.getConclusioni());
        }
    }

    @Nested
    @DisplayName("Test Costruttori")
    class ConstructorTests {

        @Test
        @DisplayName("Costruttore NoArgs")
        void testNoArgsConstructor() {
            Referto referto = new Referto();

            assertEquals(0, referto.getId());
            assertNull(referto.getNomePaziente());
            assertNull(referto.getCodiceFiscale());
            assertNull(referto.getTipoEsame());
        }

        @Test
        @DisplayName("Costruttore AllArgs")
        void testAllArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();

            Referto referto = new Referto(
                    1,
                    "Mario Rossi",
                    "RSSMRA80A01H501Z",
                    TipoEsame.TAC,
                    "Testo",
                    "Conclusioni",
                    "http://img.png",
                    "http://pdf.pdf",
                    "referto",
                    "dott@test.com",
                    now
            );

            assertEquals(1, referto.getId());
            assertEquals("Mario Rossi", referto.getNomePaziente());
            assertEquals(TipoEsame.TAC, referto.getTipoEsame());
        }
    }

    @Nested
    @DisplayName("Test con tutti i tipi di esame")
    class TipiEsameTests {

        @Test
        @DisplayName("Test con TipoEsame TAC")
        void testWithTAC() {
            Referto referto = Referto.builder()
                    .tipoEsame(TipoEsame.TAC)
                    .build();
            assertEquals(TipoEsame.TAC, referto.getTipoEsame());
        }

        @Test
        @DisplayName("Test con TipoEsame Radiografia")
        void testWithRadiografia() {
            Referto referto = Referto.builder()
                    .tipoEsame(TipoEsame.Radiografia)
                    .build();
            assertEquals(TipoEsame.Radiografia, referto.getTipoEsame());
        }

        @Test
        @DisplayName("Test con TipoEsame Ecografia")
        void testWithEcografia() {
            Referto referto = Referto.builder()
                    .tipoEsame(TipoEsame.Ecografia)
                    .build();
            assertEquals(TipoEsame.Ecografia, referto.getTipoEsame());
        }

        @Test
        @DisplayName("Test con TipoEsame Risonanza")
        void testWithRisonanza() {
            Referto referto = Referto.builder()
                    .tipoEsame(TipoEsame.Risonanza)
                    .build();
            assertEquals(TipoEsame.Risonanza, referto.getTipoEsame());
        }

        @Test
        @DisplayName("Test con TipoEsame Esami_Laboratorio")
        void testWithEsamiLaboratorio() {
            Referto referto = Referto.builder()
                    .tipoEsame(TipoEsame.Esami_Laboratorio)
                    .build();
            assertEquals(TipoEsame.Esami_Laboratorio, referto.getTipoEsame());
        }
    }
}
