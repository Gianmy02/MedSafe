package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.exception.RefertoNotFoundException;
import it.unisa.project.medsafe.repository.RefertoRepository;
import it.unisa.project.medsafe.utils.RefertoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefertoServiceTest {

    @InjectMocks
    private RefertoServiceImpl refertoService;

    @Mock
    private RefertoRepository refertoRepository;

    @Mock
    private RefertoMapper refertoMapper;

    @Mock
    private PdfService pdfService;

    @Mock
    private BlobStorageService blobStorageService;

    @Mock
    private AuthorizationService authorizationService;

    @Nested
    class Incorrect {

        @Test
        public void editRefertoNotFoundTest() {
            RefertoDTO dto = RefertoDTO.builder().id(1).build();
            when(refertoRepository.existsById(1)).thenReturn(false);
            boolean result = refertoService.editReferto(dto, null);
            assertFalse(result);
            verify(refertoRepository).existsById(1);
            verify(refertoRepository, never()).save(any());
        }

        @Test
        public void removeRefertoNotFoundTest() {
            when(refertoRepository.existsById(999)).thenReturn(false);
            boolean result = refertoService.removeReferto(999);
            assertFalse(result);
            verify(refertoRepository).existsById(999);
            verify(refertoRepository, never()).deleteById(anyInt());
        }

        @Test
        public void getRefertoByIdNotFoundTest() {
            when(refertoRepository.findById(999)).thenReturn(Optional.empty());
            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertoById(999));
            verify(refertoRepository).findById(999);
        }

        @Test
        public void getRefertoByCodiceFiscaleEmptyTest() {
            String cf = "INVALIDCF";
            when(refertoRepository.findByCodiceFiscale(cf)).thenReturn(List.of());
            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertoByCodiceFiscale(cf));
            verify(refertoRepository).findByCodiceFiscale(cf);
        }

        @Test
        public void getRefertoByNomeFileNotFoundTest() {
            String nomeFile = "notfound.pdf";
            when(refertoRepository.findByNomeFile(nomeFile)).thenReturn(null);
            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertoByNomeFile(nomeFile));
            verify(refertoRepository).findByNomeFile(nomeFile);
        }

        @Test
        public void getRefertiByTipoEsameEmptyTest() {
            TipoEsame tipo = TipoEsame.TAC;
            when(refertoRepository.findByTipoEsame(tipo)).thenReturn(List.of());
            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertiByTipoEsame(tipo));
            verify(refertoRepository).findByTipoEsame(tipo);
        }

        @Test
        public void getRefertiByAutoreEmailEmptyTest() {
            String email = "notfound@hospital.com";
            when(refertoRepository.findByAutoreEmail(email)).thenReturn(List.of());
            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertiByAutoreEmail(email));
            verify(refertoRepository).findByAutoreEmail(email);
        }

        @Test
        public void addRefertoIOExceptionTest() throws IOException {
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .nomeFile("test_referto")
                    .build();
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test.pdf");
            when(blobStorageService.uploadFile(any(), any())).thenThrow(new IOException("Errore upload"));

            assertThrows(RuntimeException.class, () -> {
                refertoService.addReferto(dto, file);
            });
        }
    }

    @Nested
    class Correct {

        @Test
        public void addRefertoSuccessTest() throws IOException {
            RefertoDTO dto = RefertoDTO.builder()
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .testoReferto("Esame negativo")
                    .conclusioni("Nessuna anomalia")
                    .autoreEmail("medico@hospital.com")
                    .nomeFile("referto_rossi")
                    .build();

            Referto referto = Referto.builder().id(1).build();
            MultipartFile file = mock(MultipartFile.class);
            ByteArrayInputStream pdfStream = new ByteArrayInputStream(new byte[0]);

            when(file.getOriginalFilename()).thenReturn("immagine.png");
            when(blobStorageService.uploadFile(any(), any())).thenReturn("http://blob/immagine.png");
            when(pdfService.generaPdf(any())).thenReturn(pdfStream);
            when(blobStorageService.uploadPdf(any(), any())).thenReturn("http://blob/referto.pdf");
            when(refertoMapper.refertoDTOToReferto(any())).thenReturn(referto);

            refertoService.addReferto(dto, file);

            verify(blobStorageService).uploadFile(file, "immagine.png");
            verify(pdfService).generaPdf(any());
            verify(blobStorageService).uploadPdf(any(), eq("referto_rossi"));
            verify(refertoRepository).save(referto);
        }

        @Test
        public void editRefertoSuccessTest() throws IOException {
            RefertoDTO dto = RefertoDTO.builder().id(1).autoreEmail("test@medsafe.local").nomeFile("test_referto")
                    .build();
            Referto referto = Referto.builder().id(1).autoreEmail("test@medsafe.local").build();

            when(refertoRepository.existsById(1)).thenReturn(true);
            when(refertoRepository.findById(1)).thenReturn(Optional.of(referto));
            doNothing().when(authorizationService).checkCanModifyReferto(referto, "modificare");

            // editReferto now regenerates PDF
            ByteArrayInputStream pdfStream = new ByteArrayInputStream(new byte[0]);
            when(pdfService.generaPdf(any())).thenReturn(pdfStream);
            when(blobStorageService.uploadPdf(any(), any())).thenReturn("http://blob/referto.pdf");

            when(refertoRepository.save(referto)).thenReturn(referto);

            boolean result = refertoService.editReferto(dto, null);

            assertTrue(result);
            verify(refertoRepository).existsById(1);
            verify(refertoRepository).findById(1);
            verify(authorizationService).checkCanModifyReferto(referto, "modificare");
            verify(refertoMapper).updateRefertoFromDTO(dto, referto);
            verify(refertoRepository).save(referto);
        }

        @Test
        public void removeRefertoSuccessTest() {
            Referto referto = Referto.builder().id(1).autoreEmail("test@medsafe.local").build();

            when(refertoRepository.existsById(1)).thenReturn(true);
            when(refertoRepository.findById(1)).thenReturn(Optional.of(referto));
            doNothing().when(authorizationService).checkCanModifyReferto(referto, "eliminare");
            doNothing().when(refertoRepository).deleteById(1);

            boolean result = refertoService.removeReferto(1);

            assertTrue(result);
            verify(refertoRepository).existsById(1);
            verify(refertoRepository).findById(1);
            verify(authorizationService).checkCanModifyReferto(referto, "eliminare");
            verify(refertoRepository).deleteById(1);
        }

        @Test
        public void getRefertoByIdSuccessTest() {
            Referto referto = Referto.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .build();

            RefertoDTO dto = RefertoDTO.builder()
                    .id(1)
                    .nomePaziente("Mario Rossi")
                    .codiceFiscale("RSSMRA80A01H501Z")
                    .tipoEsame(TipoEsame.TAC)
                    .build();

            when(refertoRepository.findById(1)).thenReturn(Optional.of(referto));
            when(refertoMapper.refertoToRefertoDTO(referto)).thenReturn(dto);

            RefertoDTO result = refertoService.getRefertoById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Mario Rossi", result.getNomePaziente());
            verify(refertoRepository).findById(1);
            verify(refertoMapper).refertoToRefertoDTO(referto);
        }

        @Test
        public void getRefertoByCodiceFiscaleSuccessTest() {
            String cf = "RSSMRA80A01H501Z";
            Referto referto1 = Referto.builder().id(1).codiceFiscale(cf).build();
            Referto referto2 = Referto.builder().id(2).codiceFiscale(cf).build();
            List<Referto> referti = Arrays.asList(referto1, referto2);

            RefertoDTO dto1 = RefertoDTO.builder().id(1).codiceFiscale(cf).build();
            RefertoDTO dto2 = RefertoDTO.builder().id(2).codiceFiscale(cf).build();

            when(refertoRepository.findByCodiceFiscale(cf)).thenReturn(referti);
            when(refertoMapper.refertoToRefertoDTO(referto1)).thenReturn(dto1);
            when(refertoMapper.refertoToRefertoDTO(referto2)).thenReturn(dto2);

            List<RefertoDTO> result = refertoService.getRefertoByCodiceFiscale(cf);

            assertEquals(2, result.size());
            verify(refertoRepository).findByCodiceFiscale(cf);
        }

        @Test
        public void getRefertoByNomeFileSuccessTest() {
            String nomeFile = "referto_rossi";
            Referto referto = Referto.builder().id(1).nomeFile(nomeFile).build();
            RefertoDTO dto = RefertoDTO.builder().id(1).nomeFile(nomeFile).build();

            when(refertoRepository.findByNomeFile(nomeFile)).thenReturn(referto);
            when(refertoMapper.refertoToRefertoDTO(referto)).thenReturn(dto);

            RefertoDTO result = refertoService.getRefertoByNomeFile(nomeFile);

            assertNotNull(result);
            assertEquals(nomeFile, result.getNomeFile());
            verify(refertoRepository).findByNomeFile(nomeFile);
        }

        @Test
        public void getRefertiByTipoEsameSuccessTest() {
            TipoEsame tipo = TipoEsame.TAC;
            Referto referto1 = Referto.builder().id(1).tipoEsame(tipo).build();
            Referto referto2 = Referto.builder().id(2).tipoEsame(tipo).build();
            List<Referto> referti = Arrays.asList(referto1, referto2);

            RefertoDTO dto1 = RefertoDTO.builder().id(1).tipoEsame(tipo).build();
            RefertoDTO dto2 = RefertoDTO.builder().id(2).tipoEsame(tipo).build();

            when(refertoRepository.findByTipoEsame(tipo)).thenReturn(referti);
            when(refertoMapper.refertoToRefertoDTO(referto1)).thenReturn(dto1);
            when(refertoMapper.refertoToRefertoDTO(referto2)).thenReturn(dto2);

            List<RefertoDTO> result = refertoService.getRefertiByTipoEsame(tipo);

            assertEquals(2, result.size());
            verify(refertoRepository).findByTipoEsame(tipo);
        }

        @Test
        public void getRefertiByAutoreEmailSuccessTest() {
            String email = "medico@hospital.com";
            Referto referto1 = Referto.builder().id(1).autoreEmail(email).build();
            Referto referto2 = Referto.builder().id(2).autoreEmail(email).build();
            List<Referto> referti = Arrays.asList(referto1, referto2);

            RefertoDTO dto1 = RefertoDTO.builder().id(1).autoreEmail(email).build();
            RefertoDTO dto2 = RefertoDTO.builder().id(2).autoreEmail(email).build();

            when(refertoRepository.findByAutoreEmail(email)).thenReturn(referti);
            when(refertoMapper.refertoToRefertoDTO(referto1)).thenReturn(dto1);
            when(refertoMapper.refertoToRefertoDTO(referto2)).thenReturn(dto2);

            List<RefertoDTO> result = refertoService.getRefertiByAutoreEmail(email);

            assertEquals(2, result.size());
            verify(refertoRepository).findByAutoreEmail(email);
        }

        @Test
        public void getAllRefertiSuccessTest() {
            Referto referto1 = Referto.builder().id(1).nomePaziente("Mario Rossi").build();
            Referto referto2 = Referto.builder().id(2).nomePaziente("Anna Verdi").build();
            Referto referto3 = Referto.builder().id(3).nomePaziente("Luigi Bianchi").build();
            List<Referto> referti = Arrays.asList(referto1, referto2, referto3);

            RefertoDTO dto1 = RefertoDTO.builder().id(1).nomePaziente("Mario Rossi").build();
            RefertoDTO dto2 = RefertoDTO.builder().id(2).nomePaziente("Anna Verdi").build();
            RefertoDTO dto3 = RefertoDTO.builder().id(3).nomePaziente("Luigi Bianchi").build();
            List<RefertoDTO> dtos = Arrays.asList(dto1, dto2, dto3);

            when(refertoRepository.findAll()).thenReturn(referti);
            when(refertoMapper.refertiToRefertiDTO(referti)).thenReturn(dtos);

            List<RefertoDTO> result = refertoService.getAllReferti();

            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("Mario Rossi", result.get(0).getNomePaziente());
            assertEquals("Anna Verdi", result.get(1).getNomePaziente());
            assertEquals("Luigi Bianchi", result.get(2).getNomePaziente());
            verify(refertoRepository).findAll();
            verify(refertoMapper).refertiToRefertiDTO(referti);
        }
    }

    @Nested
    @DisplayName("Test ExtraCorrect - branch coverage")
    class ExtraCorrect {

        @Test
        @DisplayName("removeReferto con id esistente rimuove e ritorna true")
        void testRemoveRefertoSuccess() {
            Referto referto = Referto.builder().id(1).autoreEmail("medico@test.com")
                    .fileUrlImmagine("https://account.blob.core.windows.net/upload-dir/imgs/img.jpg")
                    .urlPdfGenerato("https://account.blob.core.windows.net/upload-dir/pdfs/doc.pdf")
                    .build();

            when(refertoRepository.existsById(1)).thenReturn(true);
            when(refertoRepository.findById(1)).thenReturn(Optional.of(referto));
            doNothing().when(authorizationService).checkCanModifyReferto(referto, "eliminare");

            boolean result = refertoService.removeReferto(1);

            assertTrue(result);
            verify(refertoRepository).deleteById(1);
        }

        @Test
        @DisplayName("removeReferto con id inesistente ritorna false")
        void testRemoveRefertoNonTrovato() {
            when(refertoRepository.existsById(999)).thenReturn(false);

            boolean result = refertoService.removeReferto(999);

            assertFalse(result);
            verify(refertoRepository, never()).deleteById(anyInt());
        }

        @Test
        @DisplayName("addReferto propaga IOException come RuntimeException")
        void testAddRefertoIOException() throws IOException {
            RefertoDTO dto = RefertoDTO.builder().nomeFile("test").build();
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("img.jpg");

            doNothing().when(authorizationService).checkCanAddReferto();
            when(blobStorageService.uploadFile(any(), any())).thenThrow(new IOException("Errore upload"));

            assertThrows(RuntimeException.class, () -> refertoService.addReferto(dto, file));
        }

        @Test
        @DisplayName("editReferto con nuovo file aggiorna immagine e PDF")
        void testEditRefertoConNuovoFile() throws IOException {
            RefertoDTO dto = RefertoDTO.builder().id(1).autoreEmail("medico@test.com")
                    .nomeFile("referto_aggiornato").build();
            Referto referto = Referto.builder().id(1).autoreEmail("medico@test.com")
                    .fileUrlImmagine("https://account.blob.core.windows.net/upload-dir/old.jpg")
                    .urlPdfGenerato("https://account.blob.core.windows.net/upload-dir/old.pdf")
                    .build();
            MultipartFile newFile = mock(MultipartFile.class);
            when(newFile.isEmpty()).thenReturn(false);
            when(newFile.getOriginalFilename()).thenReturn("new_img.jpg");

            when(refertoRepository.existsById(1)).thenReturn(true);
            when(refertoRepository.findById(1)).thenReturn(Optional.of(referto));
            doNothing().when(authorizationService).checkCanModifyReferto(referto, "modificare");
            when(blobStorageService.uploadFile(any(), any()))
                    .thenReturn("https://account.blob.core.windows.net/upload-dir/new.jpg");
            when(pdfService.generaPdf(any())).thenReturn(new ByteArrayInputStream(new byte[0]));
            when(blobStorageService.uploadPdf(any(), any()))
                    .thenReturn("https://account.blob.core.windows.net/upload-dir/new.pdf");
            when(refertoRepository.save(referto)).thenReturn(referto);

            boolean result = refertoService.editReferto(dto, newFile);

            assertTrue(result);
            verify(blobStorageService).uploadFile(any(), any());
            verify(pdfService).generaPdf(any());
        }

        @Test
        @DisplayName("getRefertoByCodiceFiscale con lista vuota lancia RefertoNotFoundException")
        void testGetRefertoByCodiceFiscaleVuota() {
            when(refertoRepository.findByCodiceFiscale("XXXXXX00X00X000X")).thenReturn(List.of());

            assertThrows(RefertoNotFoundException.class,
                    () -> refertoService.getRefertoByCodiceFiscale("XXXXXX00X00X000X"));
        }

        @Test
        @DisplayName("getRefertoByNomeFile con null lancia RefertoNotFoundException")
        void testGetRefertoByNomeFileNull() {
            when(refertoRepository.findByNomeFile("inesistente")).thenReturn(null);

            assertThrows(RefertoNotFoundException.class,
                    () -> refertoService.getRefertoByNomeFile("inesistente"));
        }

        @Test
        @DisplayName("getRefertiByTipoEsame con lista vuota lancia RefertoNotFoundException")
        void testGetRefertiByTipoEsameVuota() {
            when(refertoRepository.findByTipoEsame(TipoEsame.TAC)).thenReturn(List.of());

            assertThrows(RefertoNotFoundException.class,
                    () -> refertoService.getRefertiByTipoEsame(TipoEsame.TAC));
        }

        @Test
        @DisplayName("getRefertiByAutoreEmail con lista vuota lancia RefertoNotFoundException")
        void testGetRefertiByAutoreEmailVuota() {
            when(refertoRepository.findByAutoreEmail("nessuno@test.com")).thenReturn(List.of());

            assertThrows(RefertoNotFoundException.class,
                    () -> refertoService.getRefertiByAutoreEmail("nessuno@test.com"));
        }

        @Test
        @DisplayName("getRefertoById con id esistente ritorna DTO")
        void testGetRefertoByIdSuccess() {
            Referto referto = Referto.builder().id(5).nomePaziente("Test Paziente").build();
            RefertoDTO dto = RefertoDTO.builder().id(5).nomePaziente("Test Paziente").build();

            when(refertoRepository.findById(5)).thenReturn(Optional.of(referto));
            when(refertoMapper.refertoToRefertoDTO(referto)).thenReturn(dto);

            RefertoDTO result = refertoService.getRefertoById(5);

            assertNotNull(result);
            assertEquals(5, result.getId());
        }

        @Test
        @DisplayName("getRefertoById con id inesistente lancia RefertoNotFoundException")
        void testGetRefertoByIdNonTrovato() {
            when(refertoRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(RefertoNotFoundException.class, () -> refertoService.getRefertoById(999));
        }
    }
}
