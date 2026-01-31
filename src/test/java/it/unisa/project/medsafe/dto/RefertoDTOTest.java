package it.unisa.project.medsafe.dto;

import it.unisa.project.medsafe.BasePojoTest;
import it.unisa.project.medsafe.entinty.TipoEsame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test POJO RefertoDTO")
class RefertoDTOTest extends BasePojoTest {

    @Nested
    @DisplayName("Test Getter e Setter")
    class GetterSetterTests {

        @Test
        @DisplayName("Test getter e setter per tutti i campi")
        void testGettersAndSetters() {
            RefertoDTO dto = new RefertoDTO();
            LocalDateTime now = LocalDateTime.now();

            dto.setId(1);
            dto.setNomePaziente("Mario Rossi");
            dto.setCodiceFiscale("RSSMRA80A01H501Z");
            dto.setTipoEsame(TipoEsame.TAC);
            dto.setTestoReferto("Testo del referto");
            dto.setConclusioni("Conclusioni");
            dto.setFileUrlImmagine("http://test/img.png");
            dto.setUrlPdfGenerato("http://test/pdf.pdf");
            dto.setNomeFile("referto_rossi");
            dto.setAutoreEmail("dott@hospital.com");
            dto.setDataCaricamento(now);

            assertEquals(1, dto.getId());
            assertEquals("Mario Rossi", dto.getNomePaziente());
            assertEquals("RSSMRA80A01H501Z", dto.getCodiceFiscale());
            assertEquals(TipoEsame.TAC, dto.getTipoEsame());
            assertEquals("Testo del referto", dto.getTestoReferto());
            assertEquals("Conclusioni", dto.getConclusioni());
            assertEquals("http://test/img.png", dto.getFileUrlImmagine());
            assertEquals("http://test/pdf.pdf", dto.getUrlPdfGenerato());
            assertEquals("referto_rossi", dto.getNomeFile());
            assertEquals("dott@hospital.com", dto.getAutoreEmail());
            assertEquals(now, dto.getDataCaricamento());
        }
    }

    @Nested
    @DisplayName("Test Builder")
    class BuilderTests {

        @Test
        @DisplayName("Crea RefertoDTO con builder - tutti i campi")
        void testBuilderComplete() {
            LocalDateTime now = LocalDateTime.now();

            RefertoDTO dto = RefertoDTO.builder()
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

            assertEquals(1, dto.getId());
            assertEquals("Mario Rossi", dto.getNomePaziente());
            assertEquals("RSSMRA80A01H501Z", dto.getCodiceFiscale());
            assertEquals(TipoEsame.TAC, dto.getTipoEsame());
            assertEquals("Testo del referto", dto.getTestoReferto());
            assertEquals("Conclusioni del referto", dto.getConclusioni());
            assertEquals("http://test/img.png", dto.getFileUrlImmagine());
            assertEquals("http://test/pdf.pdf", dto.getUrlPdfGenerato());
            assertEquals("referto_rossi", dto.getNomeFile());
            assertEquals("dott@hospital.com", dto.getAutoreEmail());
            assertEquals(now, dto.getDataCaricamento());
        }

        @Test
        @DisplayName("Crea RefertoDTO con builder - campi minimi")
        void testBuilderMinimal() {
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Luigi Bianchi")
                    .codiceFiscale("BNCLGU85B02F205X")
                    .tipoEsame(TipoEsame.RADIOGRAFIA)
                    .build();

            assertEquals(0, dto.getId());
            assertEquals("Luigi Bianchi", dto.getNomePaziente());
            assertEquals("BNCLGU85B02F205X", dto.getCodiceFiscale());
            assertEquals(TipoEsame.RADIOGRAFIA, dto.getTipoEsame());
            assertNull(dto.getTestoReferto());
            assertNull(dto.getConclusioni());
        }
    }

    @Nested
    @DisplayName("Test Costruttori")
    class ConstructorTests {

        @Test
        @DisplayName("Costruttore NoArgs")
        void testNoArgsConstructor() {
            RefertoDTO dto = new RefertoDTO();

            assertEquals(0, dto.getId());
            assertNull(dto.getNomePaziente());
            assertNull(dto.getCodiceFiscale());
            assertNull(dto.getTipoEsame());
        }

        @Test
        @DisplayName("Costruttore AllArgs")
        void testAllArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();

            RefertoDTO dto = new RefertoDTO(
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

            assertEquals(1, dto.getId());
            assertEquals("Mario Rossi", dto.getNomePaziente());
            assertEquals(TipoEsame.TAC, dto.getTipoEsame());
        }
    }

    @Nested
    @DisplayName("Test Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Stesso oggetto - equals true")
        void testEqualsSameObject() {
            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Test")
                    .build();

            RefertoDTO sameReference = dto;
            assertEquals(dto, sameReference);
            assertTrue(dto.equals(dto));
        }

        @Test
        @DisplayName("Oggetti uguali - equals true")
        void testEqualsEqualObjects() {
            RefertoDTO dto1 = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .build();

            RefertoDTO dto2 = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .build();

            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Oggetti diversi - equals false")
        void testEqualsDifferentObjects() {
            RefertoDTO dto1 = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .build();

            RefertoDTO dto2 = RefertoDTO.builder()
                    .id(2)
                    .nomePaziente("Luigi Bianchi")
                    .build();

            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Confronto con null - equals false")
        void testEqualsNull() {
            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .build();

            assertNotEquals(null, dto);
        }
    }

    @Nested
    @DisplayName("Test ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString contiene i campi")
        void testToString() {
            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .build();

            String str = dto.toString();

            assertTrue(str.contains("Mario Rossi"));
            assertTrue(str.contains("RSSMRA80A01H501Z"));
            assertTrue(str.contains("TAC"));
        }
    }
}
