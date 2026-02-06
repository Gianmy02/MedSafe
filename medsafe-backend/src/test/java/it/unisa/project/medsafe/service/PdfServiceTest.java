package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.entity.User;
import it.unisa.project.medsafe.entity.UserRole;
import it.unisa.project.medsafe.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PdfServiceTest {

    @InjectMocks
    private PdfServiceImpl pdfService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Mock di default: restituisce un utente con nome completo
        User mockUser = User.builder()
                .fullName("Mario Rossi")
                .email("medico@test.com")
                .role(UserRole.MEDICO)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
    }

    @Nested
    @DisplayName("Test generazione PDF - Casi corretti")
    class Correct {

        @Test
        @DisplayName("Genera PDF con tutti i campi compilati")
        void testGeneraPdfComplete() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Esame TAC torace eseguito con mezzo di contrasto")
                    .conclusioni("Non si evidenziano alterazioni patologiche a carico del parenchima polmonare")
                    .autoreEmail("dott.bianchi@hospital.com")
                    .nomeFile("referto_rossi")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con campi minimi")
        void testGeneraPdfMinimal() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Luigi Bianchi")
                    .codiceFiscale("BNCLGU85B02F205X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con tipo esame ECOGRAFIA")
        void testGeneraPdfEcografia() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Anna Verdi")
                    .codiceFiscale("VRDNNA90C03L219Y")
                    .tipoEsame(TipoEsame.Ecografia)
                    .testoReferto("Ecografia addome completo")
                    .conclusioni("Fegato, milza e reni nella norma")
                    .autoreEmail("dott.verdi@hospital.com")
                    .nomeFile("referto_verdi_eco")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con tipo esame RISONANZA")
        void testGeneraPdfRisonanza() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Franco Neri")
                    .codiceFiscale("NREFNC75D04G273W")
                    .tipoEsame(TipoEsame.Risonanza)
                    .testoReferto("RMN encefalo con e senza mdc")
                    .conclusioni("Esame nella norma")
                    .autoreEmail("dott.neri@hospital.com")
                    .nomeFile("referto_neri_rmn")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con tipo esame Esami_Laboratorio")
        void testGeneraPdfEsamiLaboratorio() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Carla Gialli")
                    .codiceFiscale("GLLCRL88E05H501V")
                    .tipoEsame(TipoEsame.Esami_Laboratorio)
                    .testoReferto("Emocromo completo, glicemia, colesterolo")
                    .conclusioni("Valori nella norma. Lieve aumento del colesterolo LDL.")
                    .autoreEmail("dott.gialli@hospital.com")
                    .nomeFile("referto_gialli_lab")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con testo molto lungo")
        void testGeneraPdfLongText() throws IOException {
            // Arrange
            String testoLungo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(50);
            String conclusioniLunghe = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ".repeat(30);

            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Test Paziente Lungo")
                    .codiceFiscale("TSTPZN80A01H501A")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto(testoLungo)
                    .conclusioni(conclusioniLunghe)
                    .autoreEmail("dott.test@hospital.com")
                    .nomeFile("referto_lungo")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con caratteri speciali")
        void testGeneraPdfSpecialCharacters() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("José María O'Connor")
                    .codiceFiscale("CNRJSM80A01H501Z")
                    .tipoEsame(TipoEsame.Radiografia)
                    .testoReferto("Esame con note: àèìòù, €, @, #, %, &")
                    .conclusioni("Conclusioni: OK — tutto bene «ottimo»")
                    .autoreEmail("jose.oconnor@hospital.com")
                    .nomeFile("referto_special")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con firma digitale del medico")
        void testGeneraPdfConFirmaDigitale() throws IOException {
            // Arrange
            User medico = User.builder()
                    .fullName("Giuseppe Bianchi")
                    .email("giuseppe.bianchi@hospital.com")
                    .role(UserRole.MEDICO)
                    .build();
            when(userRepository.findByEmail("giuseppe.bianchi@hospital.com")).thenReturn(Optional.of(medico));

            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Paziente Test")
                    .codiceFiscale("PZNTST80A01H501X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .testoReferto("Testo referto")
                    .conclusioni("Conclusioni")
                    .autoreEmail("giuseppe.bianchi@hospital.com")
                    .nomeFile("referto_test_firma")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
            // Il PDF dovrebbe contenere la firma digitale "Dott. Giuseppe Bianchi"
            verify(userRepository).findByEmail("giuseppe.bianchi@hospital.com");
        }

        @Test
        @DisplayName("Genera PDF quando utente non trovato usa nome di default")
        void testGeneraPdfSenzaUtente() throws IOException {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Paziente Test")
                    .codiceFiscale("PZNTST80A01H501Y")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Testo")
                    .conclusioni("Conclusioni")
                    .autoreEmail("nonexistent@test.com")
                    .nomeFile("referto_no_user")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
            // Il PDF dovrebbe essere generato con nome di default
        }
    }

    @Nested
    @DisplayName("Test generazione PDF - Casi limite e valori null")
    class Incorrect {

        @Test
        @DisplayName("Genera PDF con testoReferto null")
        void testGeneraPdfNullTestoReferto() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Test Null")
                    .codiceFiscale("TSTNLL80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto(null)
                    .conclusioni("Conclusioni presenti")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con conclusioni null")
        void testGeneraPdfNullConclusioni() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Test Null Conclusioni")
                    .codiceFiscale("TSTNLL80A01H501Y")
                    .tipoEsame(TipoEsame.Ecografia)
                    .testoReferto("Testo presente")
                    .conclusioni(null)
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con nomePaziente null")
        void testGeneraPdfNullNomePaziente() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente(null)
                    .codiceFiscale("TSTNLL80A01H501X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .testoReferto("Testo")
                    .conclusioni("Conclusioni")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }

        @Test
        @DisplayName("Genera PDF con tipoEsame null")
        void testGeneraPdfNullTipoEsame() throws IOException {
            // Arrange
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Test Tipo Null")
                    .codiceFiscale("TSTNLL80A01H501W")
                    .tipoEsame(null)
                    .testoReferto("Testo")
                    .conclusioni("Conclusioni")
                    .build();

            // Act
            ByteArrayInputStream result = pdfService.generaPdf(dto);

            // Assert
            assertNotNull(result);
            assertTrue(result.available() > 0);
        }
    }
}
