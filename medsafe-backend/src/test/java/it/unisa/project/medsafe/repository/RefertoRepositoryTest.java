package it.unisa.project.medsafe.repository;

import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.TipoEsame;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefertoRepositoryTest {

    @Autowired
    private RefertoRepository refertoRepository;

    @BeforeEach
    void setUp() {
        refertoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Test CRUD base")
    class CrudTests {

        @Test
        @Order(1)
        @DisplayName("Salva un nuovo referto")
        void testSaveReferto() {
            // Arrange
            Referto referto = Referto.builder()
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Esame TAC torace")
                    .conclusioni("Nessuna anomalia")
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_rossi")
                    .autoreEmail("dott@hospital.com")
                    .build();

            // Act
            Referto saved = refertoRepository.save(referto);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(saved.getId() > 0);
            assertEquals("Mario Rossi", saved.getNomePaziente());
        }

        @Test
        @Order(2)
        @DisplayName("Trova referto per ID")
        void testFindById() {
            // Arrange
            Referto referto = Referto.builder()
                    .nomePaziente("Luigi Bianchi")
                    .codiceFiscale("BNCLGU85B02F205X")
                    .tipoEsame(TipoEsame.Radiografia)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_bianchi")
                    .autoreEmail("dott@hospital.com")
                    .build();
            Referto saved = refertoRepository.save(referto);

            // Act
            Optional<Referto> found = refertoRepository.findById(saved.getId());

            // Assert
            assertTrue(found.isPresent());
            assertEquals("Luigi Bianchi", found.get().getNomePaziente());
        }

        @Test
        @Order(3)
        @DisplayName("Elimina referto")
        void testDeleteReferto() {
            // Arrange
            Referto referto = Referto.builder()
                    .nomePaziente("Da Eliminare")
                    .codiceFiscale("ELMNRE80A01H501Z")
                    .tipoEsame(TipoEsame.Ecografia)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_delete")
                    .autoreEmail("dott@hospital.com")
                    .build();
            Referto saved = refertoRepository.save(referto);
            int id = saved.getId();

            // Act
            refertoRepository.deleteById(id);

            // Assert
            assertFalse(refertoRepository.existsById(id));
        }

        @Test
        @Order(4)
        @DisplayName("Aggiorna referto esistente")
        void testUpdateReferto() {
            // Arrange
            Referto referto = Referto.builder()
                    .nomePaziente("Nome Originale")
                    .codiceFiscale("ORGNLE80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile("referto_update")
                    .autoreEmail("dott@hospital.com")
                    .build();
            Referto saved = refertoRepository.save(referto);

            // Act
            saved.setNomePaziente("Nome Modificato");
            saved.setTestoReferto("Testo aggiornato");
            Referto updated = refertoRepository.save(saved);

            // Assert
            assertEquals("Nome Modificato", updated.getNomePaziente());
            assertEquals("Testo aggiornato", updated.getTestoReferto());
        }
    }

    @Nested
    @DisplayName("Test query personalizzate")
    class CustomQueryTests {

        @Test
        @DisplayName("Trova referti per codice fiscale")
        void testFindByCodiceFiscale() {
            // Arrange
            String cf = "TSTCF080A01H501Z";
            Referto referto1 = createReferto("Paziente 1", cf, TipoEsame.TAC, "referto1");
            Referto referto2 = createReferto("Paziente 1", cf, TipoEsame.Ecografia, "referto2");
            refertoRepository.save(referto1);
            refertoRepository.save(referto2);

            // Act
            List<Referto> result = refertoRepository.findByCodiceFiscale(cf);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.getCodiceFiscale().equals(cf)));
        }

        @Test
        @DisplayName("Trova referti per tipo esame")
        void testFindByTipoEsame() {
            // Arrange
            Referto referto1 = createReferto("Paziente TAC 1", "TSTTAC80A01H501A", TipoEsame.TAC, "referto_tac1");
            Referto referto2 = createReferto("Paziente TAC 2", "TSTTAC80A01H501B", TipoEsame.TAC, "referto_tac2");
            Referto referto3 = createReferto("Paziente ECO", "TSTECO80A01H501C", TipoEsame.Ecografia, "referto_eco");
            refertoRepository.save(referto1);
            refertoRepository.save(referto2);
            refertoRepository.save(referto3);

            // Act
            List<Referto> tacReferti = refertoRepository.findByTipoEsame(TipoEsame.TAC);
            List<Referto> ecoReferti = refertoRepository.findByTipoEsame(TipoEsame.Ecografia);

            // Assert
            assertEquals(2, tacReferti.size());
            assertEquals(1, ecoReferti.size());
        }

        @Test
        @DisplayName("Trova referti per email autore")
        void testFindByAutoreEmail() {
            // Arrange
            String email = "dott.specialista@hospital.com";
            Referto referto1 = Referto.builder()
                    .nomePaziente("Paziente 1")
                    .codiceFiscale("PZNT0180A01H501A")
                    .tipoEsame(TipoEsame.TAC)
                    .fileUrlImmagine("http://test/img1.png")
                    .urlPdfGenerato("http://test/pdf1.pdf")
                    .nomeFile("referto_email1")
                    .autoreEmail(email)
                    .build();
            Referto referto2 = Referto.builder()
                    .nomePaziente("Paziente 2")
                    .codiceFiscale("PZNT0280A01H501B")
                    .tipoEsame(TipoEsame.Radiografia)
                    .fileUrlImmagine("http://test/img2.png")
                    .urlPdfGenerato("http://test/pdf2.pdf")
                    .nomeFile("referto_email2")
                    .autoreEmail(email)
                    .build();
            refertoRepository.save(referto1);
            refertoRepository.save(referto2);

            // Act
            List<Referto> result = refertoRepository.findByAutoreEmail(email);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(r -> r.getAutoreEmail().equals(email)));
        }

        @Test
        @DisplayName("Trova referto per nome file")
        void testFindByNomeFile() {
            // Arrange
            String nomeFile = "referto_unico_test";
            Referto referto = Referto.builder()
                    .nomePaziente("Test NomeFile")
                    .codiceFiscale("TSTNMF80A01H501Z")
                    .tipoEsame(TipoEsame.Risonanza)
                    .fileUrlImmagine("http://test/img.png")
                    .urlPdfGenerato("http://test/pdf.pdf")
                    .nomeFile(nomeFile)
                    .autoreEmail("dott@hospital.com")
                    .build();
            refertoRepository.save(referto);

            // Act
            Referto found = refertoRepository.findByNomeFile(nomeFile);

            // Assert
            assertNotNull(found);
            assertEquals(nomeFile, found.getNomeFile());
        }

        @Test
        @DisplayName("Trova referto per nome file - non esistente")
        void testFindByNomeFileNotFound() {
            // Act
            Referto found = refertoRepository.findByNomeFile("non_esiste");

            // Assert
            assertNull(found);
        }

        @Test
        @DisplayName("Trova referti per codice fiscale - nessun risultato")
        void testFindByCodiceFiscaleEmpty() {
            // Act
            List<Referto> result = refertoRepository.findByCodiceFiscale("NOTEXIST");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Test con tutti i tipi di esame")
    class TipiEsameTests {

        @Test
        @DisplayName("Salva e recupera tutti i tipi di esame")
        void testAllTipiEsame() {
            // Arrange & Act
            for (TipoEsame tipo : TipoEsame.values()) {
                Referto referto = Referto.builder()
                        .nomePaziente("Test " + tipo.name())
                        .codiceFiscale(
                                "TST" + tipo.name().substring(0, Math.min(3, tipo.name().length())) + "80A01H501Z")
                        .tipoEsame(tipo)
                        .fileUrlImmagine("http://test/img_" + tipo.name() + ".png")
                        .urlPdfGenerato("http://test/pdf_" + tipo.name() + ".pdf")
                        .nomeFile("referto_" + tipo.name().toLowerCase())
                        .autoreEmail("dott@hospital.com")
                        .build();
                Referto saved = refertoRepository.save(referto);

                // Assert
                assertNotNull(saved.getId());
                assertEquals(tipo, saved.getTipoEsame());
            }

            // Verifica conteggio totale
            assertEquals(TipoEsame.values().length, refertoRepository.findAll().size());
        }
    }

    // Metodo helper per creare referti
    private Referto createReferto(String nomePaziente, String cf, TipoEsame tipo, String nomeFile) {
        return Referto.builder()
                .nomePaziente(nomePaziente)
                .codiceFiscale(cf)
                .tipoEsame(tipo)
                .fileUrlImmagine("http://test/img.png")
                .urlPdfGenerato("http://test/pdf.pdf")
                .nomeFile(nomeFile)
                .autoreEmail("dott@hospital.com")
                .build();
    }
}
