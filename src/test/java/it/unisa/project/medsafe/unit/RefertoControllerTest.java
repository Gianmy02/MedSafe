package it.unisa.project.medsafe.unit;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entinty.TipoEsame;
import it.unisa.project.medsafe.rest.RefertoController;
import it.unisa.project.medsafe.service.BlobStorageService;
import it.unisa.project.medsafe.service.RefertoService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefertoControllerTest {

    @InjectMocks
    private RefertoController refertoController;

    @Mock
    private RefertoService refertoService;

    @Mock
    private BlobStorageService blobStorageService;

    @Nested
    class Incorrect {

        @Test
        public void addRefertoInvalidFileExtensionTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("documento.txt");

            ResponseEntity<?> response = refertoController.addReferto(
                    "Mario Rossi",
                    "RSSMRA80A01H501Z",
                    TipoEsame.TAC,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_test",
                    file
            );

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("Formato file non supportato"));
        }

        @Test
        public void addRefertoEmptyFilenameTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("");

            ResponseEntity<?> response = refertoController.addReferto(
                    "Mario Rossi",
                    "RSSMRA80A01H501Z",
                    TipoEsame.TAC,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_test",
                    file
            );

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Nome file non valido", response.getBody());
        }

        @Test
        public void addRefertoNullFilenameTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn(null);

            ResponseEntity<?> response = refertoController.addReferto(
                    "Mario Rossi",
                    "RSSMRA80A01H501Z",
                    TipoEsame.TAC,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_test",
                    file
            );

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        public void editRefertoNotFoundTest() {
            RefertoDTO dto = RefertoDTO.builder().id(999).build();
            when(refertoService.editReferto(dto)).thenReturn(false);

            ResponseEntity<Void> response = refertoController.editReferto(dto);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(refertoService).editReferto(dto);
        }

        @Test
        public void removeRefertoNotFoundTest() {
            when(refertoService.removeReferto(999)).thenReturn(false);

            ResponseEntity<Void> response = refertoController.removeReferto(999);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(refertoService).removeReferto(999);
        }

        @Test
        public void getRefertoByNomeFileNotFoundTest() {
            when(refertoService.getRefertoByNomeFile("nonexistent")).thenReturn(null);

            ResponseEntity<RefertoDTO> response = refertoController.getRefertoByNomeFile("nonexistent");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(refertoService).getRefertoByNomeFile("nonexistent");
        }

        @Test
        public void downloadPdfNotFoundTest() {
            when(refertoService.getRefertoById(999)).thenReturn(null);

            ResponseEntity<byte[]> response = refertoController.downloadPdf(999);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        public void downloadImmagineNotFoundTest() {
            when(refertoService.getRefertoById(999)).thenReturn(null);

            ResponseEntity<byte[]> response = refertoController.downloadImmagine(999);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    class Correct {

        @Test
        public void addRefertoSuccessPdfTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("documento.pdf");
            when(file.getSize()).thenReturn(1024L);
            doNothing().when(refertoService).addReferto(any(), any());

            ResponseEntity<?> response = refertoController.addReferto(
                    "Mario Rossi",
                    "RSSMRA80A01H501Z",
                    TipoEsame.TAC,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_test",
                    file
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(refertoService).addReferto(any(), eq(file));
        }

        @Test
        public void addRefertoSuccessPngTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("immagine.png");
            when(file.getSize()).thenReturn(2048L);
            doNothing().when(refertoService).addReferto(any(), any());

            ResponseEntity<?> response = refertoController.addReferto(
                    "Luigi Bianchi",
                    "BNCLGU85B02F205X",
                    TipoEsame.RADIOGRAFIA,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_bianchi",
                    file
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        public void addRefertoSuccessJpgTest() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("immagine.jpg");
            when(file.getSize()).thenReturn(3072L);
            doNothing().when(refertoService).addReferto(any(), any());

            ResponseEntity<?> response = refertoController.addReferto(
                    "Anna Verdi",
                    "VRDNNA90C03L219Y",
                    TipoEsame.ECOGRAFIA,
                    "Testo referto",
                    "Conclusioni",
                    "medico@hospital.com",
                    "referto_verdi",
                    file
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        public void editRefertoSuccessTest() {
            RefertoDTO dto = RefertoDTO.builder().id(1).build();
            when(refertoService.editReferto(dto)).thenReturn(true);

            ResponseEntity<Void> response = refertoController.editReferto(dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(refertoService).editReferto(dto);
        }

        @Test
        public void removeRefertoSuccessTest() {
            when(refertoService.removeReferto(1)).thenReturn(true);

            ResponseEntity<Void> response = refertoController.removeReferto(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(refertoService).removeReferto(1);
        }

        @Test
        public void getRefertoByCodiceFiscaleSuccessTest() {
            String cf = "RSSMRA80A01H501Z";
            List<RefertoDTO> referti = Arrays.asList(
                    RefertoDTO.builder().id(1).codiceFiscale(cf).build(),
                    RefertoDTO.builder().id(2).codiceFiscale(cf).build()
            );
            when(refertoService.getRefertoByCodiceFiscale(cf)).thenReturn(referti);

            ResponseEntity<List<RefertoDTO>> response = refertoController.getRefertoByCodiceFiscale(cf);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            verify(refertoService).getRefertoByCodiceFiscale(cf);
        }

        @Test
        public void getRefertoByNomeFileSuccessTest() {
            String nomeFile = "referto_rossi";
            RefertoDTO dto = RefertoDTO.builder().id(1).nomeFile(nomeFile).build();
            when(refertoService.getRefertoByNomeFile(nomeFile)).thenReturn(dto);

            ResponseEntity<RefertoDTO> response = refertoController.getRefertoByNomeFile(nomeFile);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(nomeFile, response.getBody().getNomeFile());
            verify(refertoService).getRefertoByNomeFile(nomeFile);
        }

        @Test
        public void getRefertiByTipoEsameSuccessTest() {
            TipoEsame tipo = TipoEsame.TAC;
            List<RefertoDTO> referti = Arrays.asList(
                    RefertoDTO.builder().id(1).tipoEsame(tipo).build(),
                    RefertoDTO.builder().id(2).tipoEsame(tipo).build()
            );
            when(refertoService.getRefertiByTipoEsame(tipo)).thenReturn(referti);

            ResponseEntity<List<RefertoDTO>> response = refertoController.getRefertiByTipoEsame(tipo);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            verify(refertoService).getRefertiByTipoEsame(tipo);
        }

        @Test
        public void getRefertiByAutoreEmailSuccessTest() {
            String email = "medico@hospital.com";
            List<RefertoDTO> referti = Arrays.asList(
                    RefertoDTO.builder().id(1).autoreEmail(email).build(),
                    RefertoDTO.builder().id(2).autoreEmail(email).build()
            );
            when(refertoService.getRefertiByAutoreEmail(email)).thenReturn(referti);

            ResponseEntity<List<RefertoDTO>> response = refertoController.getRefertiByAutoreEmail(email);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            verify(refertoService).getRefertiByAutoreEmail(email);
        }

        @Test
        public void downloadPdfSuccessTest() {
            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .nomeFile("referto_rossi.pdf")
                    .urlPdfGenerato("http://127.0.0.1:10000/devstoreaccount1/referti/pdf/referto.pdf")
                    .build();
            byte[] pdfContent = "PDF content".getBytes();

            when(refertoService.getRefertoById(1)).thenReturn(dto);
            when(blobStorageService.downloadFile(anyString())).thenReturn(pdfContent);

            ResponseEntity<byte[]> response = refertoController.downloadPdf(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(refertoService).getRefertoById(1);
        }

        @Test
        public void downloadImmagineSuccessTest() {
            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .nomeFile("immagine.png")
                    .fileUrlImmagine("http://127.0.0.1:10000/devstoreaccount1/referti/immagini/immagine.png")
                    .build();
            byte[] imageContent = "Image content".getBytes();

            when(refertoService.getRefertoById(1)).thenReturn(dto);
            when(blobStorageService.downloadFile(anyString())).thenReturn(imageContent);

            ResponseEntity<byte[]> response = refertoController.downloadImmagine(1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(refertoService).getRefertoById(1);
        }
    }
}
