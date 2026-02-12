package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.Referto;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.exception.RefertoNotFoundException;
import it.unisa.project.medsafe.repository.RefertoRepository;
import it.unisa.project.medsafe.utils.RefertoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RefertoServiceImpl implements RefertoService {
    private final RefertoRepository refertoRepository;
    private final RefertoMapper refertoMapper;
    private final PdfService pdfService;
    private final BlobStorageService blobStorageService;
    private final AuthorizationService authorizationService;

    public void addReferto(RefertoDTO dto, MultipartFile file) {
        // 🔐 CONTROLLO AUTORIZZAZIONE: verifica se l'utente è abilitato
        authorizationService.checkCanAddReferto();

        // Setta i metadati (Fuso orario italiano)
        dto.setDataCaricamento(LocalDateTime.now(java.time.ZoneId.of("Europe/Rome")));

        try {
            // 1. Carica l'immagine originale su Azure Blob Storage
            String imageUrl = blobStorageService.uploadFile(file, file.getOriginalFilename());
            dto.setFileUrlImmagine(imageUrl);

            // 2. Genera il PDF con i dati del referto
            ByteArrayInputStream pdfStream = pdfService.generaPdf(dto);

            // 3. Carica il PDF generato su Azure Blob Storage con il nome scelto
            // dall'utente
            String pdfUrl = blobStorageService.uploadPdf(pdfStream, dto.getNomeFile());
            dto.setUrlPdfGenerato(pdfUrl);

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento dei file: " + e.getMessage(), e);
        }

        refertoRepository.save(refertoMapper.refertoDTOToReferto(dto));
    }

    public boolean editReferto(RefertoDTO dto, MultipartFile file) {
        log.info("Inizio procedura modifica referto ID: {}", dto.getId());

        if (refertoRepository.existsById(dto.getId())) {
            // Recupera il referto esistente
            Referto referto = refertoRepository.findById(dto.getId())
                    .orElseThrow(() -> new RefertoNotFoundException("Referto non trovato con ID: " + dto.getId()));

            // 🔐 CONTROLLO AUTORIZZAZIONE: solo il proprietario o un admin può modificare
            authorizationService.checkCanModifyReferto(referto, "modificare");

            // Manteniamo i vecchi URL per l'eliminazione successiva
            String oldImgUrl = referto.getFileUrlImmagine();
            String oldPdfUrl = referto.getUrlPdfGenerato();

            // 1. Gestione Immagine: se c'è un nuovo file, caricalo ed elimina il vecchio
            if (file != null && !file.isEmpty()) {
                log.info("Nuova immagine rilevata, procedo con upload...");
                try {
                    String newImgUrl = blobStorageService.uploadFile(file, file.getOriginalFilename());
                    dto.setFileUrlImmagine(newImgUrl);
                    log.info("Nuova immagine caricata: {}", newImgUrl);

                    // Se l'URL è cambiato (dovrebbe esserlo), elimina la vecchia immagine
                    if (oldImgUrl != null && !oldImgUrl.equals(newImgUrl)) {
                        String oldImgBlobName = extractBlobPathFromUrl(oldImgUrl);
                        if (oldImgBlobName != null) {
                            blobStorageService.deleteFile(oldImgBlobName);
                            log.info("Vecchia immagine eliminata: {}", oldImgBlobName);
                        }
                    }
                } catch (IOException e) {
                    log.error("Errore upload nuova immagine", e);
                    throw new RuntimeException("Errore durante il caricamento della nuova immagine: " + e.getMessage(),
                            e);
                }
            } else {
                log.info("Nessuna nuova immagine caricata. Mantengo l'immagine esistente.");
                // Se non c'è nuovo file, mantieni l'URL esistente nel DTO per coerenza
                // Se il DTO ha il campo vuoto, lo riempiamo con quello del DB
                if (dto.getFileUrlImmagine() == null || dto.getFileUrlImmagine().isEmpty()) {
                    log.info("Ripristino URL immagine dal DB: {}", oldImgUrl);
                    dto.setFileUrlImmagine(oldImgUrl);
                }
            }

            try {
                // 2. Rigenera il PDF con i nuovi dati
                log.info("Rigenerazione PDF referto...");
                ByteArrayInputStream pdfStream = pdfService.generaPdf(dto);

                // 3. Carica il nuovo PDF
                // Usiamo il nome file aggiornato (o lo stesso) per generare un nuovo blob unico
                String newPdfUrl = blobStorageService.uploadPdf(pdfStream, dto.getNomeFile());
                dto.setUrlPdfGenerato(newPdfUrl);
                log.info("Nuovo PDF generato e caricato: {}", newPdfUrl);

                // 4. Elimina il vecchio PDF
                String oldPdfBlobName = extractBlobPathFromUrl(oldPdfUrl);
                if (oldPdfBlobName != null) {
                    blobStorageService.deleteFile(oldPdfBlobName);
                    log.info("Vecchio PDF eliminato: {}", oldPdfBlobName);
                }

            } catch (IOException e) {
                log.error("Errore rigenerazione PDF", e);
                throw new RuntimeException("Errore durante la rigenerazione del PDF: " + e.getMessage(), e);
            }

            // Se autorizzato, procedi con la modifica
            // Aggiorna l'entità esistente con i dati del DTO (mantiene ID e
            // dataCaricamento)
            log.info("Aggiornamento entità Referto sul DB...");
            refertoMapper.updateRefertoFromDTO(dto, referto);
            refertoRepository.save(referto);
            log.info("Referto salvato correttamente.");
            return true;
        }
        log.warn("Tentativo di modifica fallito: Referto ID {} non trovato.", dto.getId());
        return false;

    }

    /**
     * Estrae il percorso del blob dall'URL completo.
     * Assumendo formato: https://<account>.blob.core.windows.net/<container>/<path>
     * Il container è "upload-dir".
     */
    private String extractBlobPathFromUrl(String url) {
        if (url == null || url.isEmpty())
            return null;
        try {
            String containerName = "upload-dir";
            int index = url.indexOf("/" + containerName + "/");
            if (index != -1) {
                String encodedPath = url.substring(index + containerName.length() + 2);
                return java.net.URLDecoder.decode(encodedPath, java.nio.charset.StandardCharsets.UTF_8);
            }
            // Tentativo fallback se inizia con container
            if (url.startsWith(containerName + "/")) {
                String encodedPath = url.substring(containerName.length() + 1);
                return java.net.URLDecoder.decode(encodedPath, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // Logghiamo l'errore ma non blocchiamo tutto per un fallimento di cleanup
            System.err.println("Errore estrazione blob path: " + e.getMessage());
        }
        return null;
    }

    public boolean removeReferto(int id) {
        if (refertoRepository.existsById(id)) {
            // Recupera il referto esistente
            Referto referto = refertoRepository.findById(id)
                    .orElseThrow(() -> new RefertoNotFoundException("Referto non trovato con ID: " + id));

            // 🔐 CONTROLLO AUTORIZZAZIONE: solo il proprietario o un admin può eliminare
            authorizationService.checkCanModifyReferto(referto, "eliminare");

            // Se autorizzato, procedi con l'eliminazione
            try {
                // Elimina immagine se presente
                String imgBlobName = extractBlobPathFromUrl(referto.getFileUrlImmagine());
                if (imgBlobName != null) {
                    blobStorageService.deleteFile(imgBlobName);
                }

                // Elimina PDF se presente
                String pdfBlobName = extractBlobPathFromUrl(referto.getUrlPdfGenerato());
                if (pdfBlobName != null) {
                    blobStorageService.deleteFile(pdfBlobName);
                }
            } catch (Exception e) {
                // Logghiamo l'errore ma procediamo con l'eliminazione del record DB
                // per evitare disallineamenti gravi (o decidere se bloccare, ma cleanup
                // è meglio best-effort)
                System.err.println("Errore durante l'eliminazione dei file da BlobStorage: " + e.getMessage());
            }

            refertoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<RefertoDTO> getRefertoByCodiceFiscale(String codiceFiscale) {
        var list = refertoRepository.findByCodiceFiscale(codiceFiscale);
        if (list == null || list.isEmpty()) {
            throw new RefertoNotFoundException("Nessun referto trovato per il codice fiscale: " + codiceFiscale);
        }
        return list.stream().map(refertoMapper::refertoToRefertoDTO).toList();
    }

    public RefertoDTO getRefertoByNomeFile(String nomeFile) {
        var referto = refertoRepository.findByNomeFile(nomeFile);
        if (referto == null) {
            throw new RefertoNotFoundException("Referto non trovato per nome file: " + nomeFile);
        }
        return refertoMapper.refertoToRefertoDTO(referto);
    }

    public List<RefertoDTO> getRefertiByTipoEsame(TipoEsame tipoEsame) {
        var list = refertoRepository.findByTipoEsame(tipoEsame);
        if (list == null || list.isEmpty()) {
            throw new RefertoNotFoundException("Nessun referto trovato per tipo esame: " + tipoEsame);
        }
        return list.stream().map(refertoMapper::refertoToRefertoDTO).toList();
    }

    public List<RefertoDTO> getRefertiByAutoreEmail(String autoreEmail) {
        var list = refertoRepository.findByAutoreEmail(autoreEmail);
        if (list == null || list.isEmpty()) {
            throw new RefertoNotFoundException("Nessun referto trovato per autore: " + autoreEmail);
        }
        return list.stream().map(refertoMapper::refertoToRefertoDTO).toList();
    }

    @Override
    public RefertoDTO getRefertoById(int id) {
        return refertoRepository.findById(id)
                .map(refertoMapper::refertoToRefertoDTO)
                .orElseThrow(() -> new RefertoNotFoundException("Referto non trovato per id: " + id));
    }

    @Override
    public List<RefertoDTO> getAllReferti() {
        var list = refertoRepository.findAll();
        return refertoMapper.refertiToRefertiDTO(list);
    }

}
