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

@Service
@Transactional
@RequiredArgsConstructor
public class RefertoServiceImpl implements RefertoService {
    private final RefertoRepository refertoRepository;
    private final RefertoMapper refertoMapper;
    private final PdfService pdfService;
    private final BlobStorageService blobStorageService;
    private final AuthorizationService authorizationService;

    public void addReferto(RefertoDTO dto, MultipartFile file) {
        // Setta i metadati
        dto.setDataCaricamento(LocalDateTime.now());

        try {
            // 1. Carica l'immagine originale su Azure Blob Storage
            String imageUrl = blobStorageService.uploadFile(file, file.getOriginalFilename());
            dto.setFileUrlImmagine(imageUrl);

            // 2. Genera il PDF con i dati del referto
            ByteArrayInputStream pdfStream = pdfService.generaPdf(dto);

            // 3. Carica il PDF generato su Azure Blob Storage con il nome scelto dall'utente
            String pdfUrl = blobStorageService.uploadPdf(pdfStream, dto.getNomeFile());
            dto.setUrlPdfGenerato(pdfUrl);

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento dei file: " + e.getMessage(), e);
        }

        refertoRepository.save(refertoMapper.refertoDTOToReferto(dto));
    }

    public boolean editReferto(RefertoDTO dto) {
        if(refertoRepository.existsById(dto.getId())) {
            // Recupera il referto esistente
            Referto referto = refertoRepository.findById(dto.getId())
                    .orElseThrow(() -> new RefertoNotFoundException("Referto non trovato con ID: " + dto.getId()));

            // 🔐 CONTROLLO AUTORIZZAZIONE: solo il proprietario o un admin può modificare
            authorizationService.checkCanModifyReferto(referto, "modificare");

            // Se autorizzato, procedi con la modifica
            refertoRepository.save(refertoMapper.refertoDTOToReferto(dto));
            return  true;
        }
        return false;
    }

    public boolean removeReferto(int id) {
        if(refertoRepository.existsById(id)) {
            // Recupera il referto esistente
            Referto referto = refertoRepository.findById(id)
                    .orElseThrow(() -> new RefertoNotFoundException("Referto non trovato con ID: " + id));

            // 🔐 CONTROLLO AUTORIZZAZIONE: solo il proprietario o un admin può eliminare
            authorizationService.checkCanModifyReferto(referto, "eliminare");

            // Se autorizzato, procedi con l'eliminazione
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
