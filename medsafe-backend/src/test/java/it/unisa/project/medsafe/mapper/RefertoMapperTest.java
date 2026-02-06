package it.unisa.project.medsafe.mapper;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.utils.RefertoMapper;
import it.unisa.project.medsafe.utils.RefertoMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RefertoMapperTest {

    private RefertoMapper refertoMapper;

    @BeforeEach
    void setUp() {
        refertoMapper = new RefertoMapperImpl();
    }

    @Nested
    @DisplayName("Test conversione Entity -> DTO")
    class EntityToDTO {

        @Test
        @DisplayName("Converte Referto completo in RefertoDTO")
        void testRefertoToRefertoDTOComplete() {
            // Arrange
            Referto referto = Referto.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Esame TAC torace")
                    .conclusioni("Nessuna anomalia")
                    .fileUrlImmagine("http://blob/immagine.png")
                    .urlPdfGenerato("http://blob/referto.pdf")
                    .nomeFile("referto_rossi")
                    .autoreEmail("dott@hospital.com")
                    .dataCaricamento(LocalDateTime.now())
                    .build();

            // Act
            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);

            // Assert
            assertNotNull(dto);
            assertEquals(1, dto.getId());
            assertEquals("Mario Rossi", dto.getNomePaziente());
            assertEquals("RSSMRA80A01H501Z", dto.getCodiceFiscale());
            assertEquals(TipoEsame.TAC, dto.getTipoEsame());
            assertEquals("Esame TAC torace", dto.getTestoReferto());
            assertEquals("Nessuna anomalia", dto.getConclusioni());
            assertEquals("http://blob/immagine.png", dto.getFileUrlImmagine());
            assertEquals("http://blob/referto.pdf", dto.getUrlPdfGenerato());
            assertEquals("referto_rossi", dto.getNomeFile());
            assertEquals("dott@hospital.com", dto.getAutoreEmail());
            assertNotNull(dto.getDataCaricamento());
        }

        @Test
        @DisplayName("Converte Referto con campi null")
        void testRefertoToRefertoDTOWithNulls() {
            // Arrange
            Referto referto = Referto.builder()
                    .id(2)
                    .nomePaziente("Luigi Bianchi")
                    .codiceFiscale("BNCLGU85B02F205X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .testoReferto(null)
                    .conclusioni(null)
                    .fileUrlImmagine("http://blob/img.png")
                    .urlPdfGenerato("http://blob/pdf.pdf")
                    .nomeFile("referto_bianchi")
                    .autoreEmail("dott@test.com")
                    .build();

            // Act
            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);

            // Assert
            assertNotNull(dto);
            assertEquals(2, dto.getId());
            assertNull(dto.getTestoReferto());
            assertNull(dto.getConclusioni());
        }

        @Test
        @DisplayName("Converte lista di Referto in lista di RefertoDTO")
        void testRefertiToRefertiDTO() {
            // Arrange
            Referto referto1 = Referto.builder()
                    .id(1)
                    .nomePaziente("Paziente 1")
                    .codiceFiscale("PZNT0180A01H501A")
                    .tipoEsame(TipoEsame.TAC)
                    .fileUrlImmagine("http://test/img1.png")
                    .urlPdfGenerato("http://test/pdf1.pdf")
                    .nomeFile("referto1")
                    .autoreEmail("dott@test.com")
                    .build();

            Referto referto2 = Referto.builder()
                    .id(2)
                    .nomePaziente("Paziente 2")
                    .codiceFiscale("PZNT0280A01H501B")
                    .tipoEsame(TipoEsame.Ecografia)
                    .fileUrlImmagine("http://test/img2.png")
                    .urlPdfGenerato("http://test/pdf2.pdf")
                    .nomeFile("referto2")
                    .autoreEmail("dott@test.com")
                    .build();

            List<Referto> referti = Arrays.asList(referto1, referto2);

            // Act
            List<RefertoDTO> dtos = refertoMapper.refertiToRefertiDTO(referti);

            // Assert
            assertNotNull(dtos);
            assertEquals(2, dtos.size());
            assertEquals("Paziente 1", dtos.get(0).getNomePaziente());
            assertEquals("Paziente 2", dtos.get(1).getNomePaziente());
        }

        @Test
        @DisplayName("Converte lista vuota")
        void testRefertiToRefertiDTOEmpty() {
            // Arrange
            List<Referto> referti = List.of();

            // Act
            List<RefertoDTO> dtos = refertoMapper.refertiToRefertiDTO(referti);

            // Assert
            assertNotNull(dtos);
            assertTrue(dtos.isEmpty());
        }
    }

    @Nested
    @DisplayName("Test conversione DTO -> Entity")
    class DTOToEntity {

        @Test
        @DisplayName("Converte RefertoDTO completo in Referto")
        void testRefertoDTOToRefertoComplete() {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .id(99) // Questo dovrebbe essere ignorato
                    .nomePaziente("Anna Verdi")
                    .codiceFiscale("VRDNNA90C03L219Y")
                    .tipoEsame(TipoEsame.Ecografia)
                    .testoReferto("Ecografia addome")
                    .conclusioni("Nella norma")
                    .fileUrlImmagine("http://blob/immagine.png")
                    .urlPdfGenerato("http://blob/referto.pdf")
                    .nomeFile("referto_verdi")
                    .autoreEmail("dott.verdi@hospital.com")
                    .dataCaricamento(LocalDateTime.now()) // Questo dovrebbe essere ignorato
                    .build();

            // Act
            Referto referto = refertoMapper.refertoDTOToReferto(dto);

            // Assert
            assertNotNull(referto);
            assertEquals(0, referto.getId()); // ID ignorato, quindi 0
            assertEquals("Anna Verdi", referto.getNomePaziente());
            assertEquals("VRDNNA90C03L219Y", referto.getCodiceFiscale());
            assertEquals(TipoEsame.Ecografia, referto.getTipoEsame());
            assertEquals("Ecografia addome", referto.getTestoReferto());
            assertEquals("Nella norma", referto.getConclusioni());
            assertNull(referto.getDataCaricamento()); // dataCaricamento ignorato
        }

        @Test
        @DisplayName("Converte RefertoDTO con campi null")
        void testRefertoDTOToRefertoWithNulls() {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Test Null")
                    .codiceFiscale("TSTNLL80A01H501Z")
                    .tipoEsame(TipoEsame.Risonanza)
                    .testoReferto(null)
                    .conclusioni(null)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_null")
                    .autoreEmail("test@test.com")
                    .build();

            // Act
            Referto referto = refertoMapper.refertoDTOToReferto(dto);

            // Assert
            assertNotNull(referto);
            assertNull(referto.getTestoReferto());
            assertNull(referto.getConclusioni());
        }

        @Test
        @DisplayName("Verifica che ID viene ignorato nella conversione")
        void testIdIgnored() {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .id(999)
                    .nomePaziente("Test ID")
                    .codiceFiscale("TSTID080A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_id")
                    .autoreEmail("test@test.com")
                    .build();

            // Act
            Referto referto = refertoMapper.refertoDTOToReferto(dto);

            // Assert
            assertEquals(0, referto.getId()); // ID deve essere 0 (default int)
        }
    }

    @Nested
    @DisplayName("Test con tutti i tipi di esame")
    class TipiEsame {

        @Test
        @DisplayName("Conversione con TipoEsame TAC")
        void testConTAC() {
            Referto referto = Referto.builder()
                    .id(1)
                    .tipoEsame(TipoEsame.TAC)
                    .nomePaziente("Test")
                    .codiceFiscale("TSTCF080A01H501Z")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("test")
                    .autoreEmail("test@test.com")
                    .build();

            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);
            assertEquals(TipoEsame.TAC, dto.getTipoEsame());
        }

        @Test
        @DisplayName("Conversione con TipoEsame RADIOGRAFIA")
        void testConRADIOGRAFIA() {
            Referto referto = Referto.builder()
                    .id(2)
                    .tipoEsame(TipoEsame.Radiografia)
                    .nomePaziente("Test")
                    .codiceFiscale("TSTCF080A01H501Y")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("test2")
                    .autoreEmail("test@test.com")
                    .build();

            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);
            assertEquals(TipoEsame.Radiografia, dto.getTipoEsame());
        }

        @Test
        @DisplayName("Conversione con TipoEsame ECOGRAFIA")
        void testConECOGRAFIA() {
            Referto referto = Referto.builder()
                    .id(3)
                    .tipoEsame(TipoEsame.Ecografia)
                    .nomePaziente("Test")
                    .codiceFiscale("TSTCF080A01H501X")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("test3")
                    .autoreEmail("test@test.com")
                    .build();

            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);
            assertEquals(TipoEsame.Ecografia, dto.getTipoEsame());
        }

        @Test
        @DisplayName("Conversione con TipoEsame RISONANZA")
        void testConRISONANZA() {
            Referto referto = Referto.builder()
                    .id(4)
                    .tipoEsame(TipoEsame.Risonanza)
                    .nomePaziente("Test")
                    .codiceFiscale("TSTCF080A01H501W")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("test4")
                    .autoreEmail("test@test.com")
                    .build();

            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);
            assertEquals(TipoEsame.Risonanza, dto.getTipoEsame());
        }

        @Test
        @DisplayName("Conversione con TipoEsame Esami_Laboratorio")
        void testConEsamiLaboratorio() {
            Referto referto = Referto.builder()
                    .id(5)
                    .tipoEsame(TipoEsame.Esami_Laboratorio)
                    .nomePaziente("Test")
                    .codiceFiscale("TSTCF080A01H501V")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("test5")
                    .autoreEmail("test@test.com")
                    .build();

            RefertoDTO dto = refertoMapper.refertoToRefertoDTO(referto);
            assertEquals(TipoEsame.Esami_Laboratorio, dto.getTipoEsame());
        }
    }
}
