package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.TipoEsame;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaccia per la gestione dei referti medici.
 */
public interface RefertoService {
    void addReferto(RefertoDTO dto, MultipartFile file);
    boolean editReferto(RefertoDTO dto, MultipartFile file);
    boolean removeReferto(int id);
    List<RefertoDTO> getRefertoByCodiceFiscale(String codiceFiscale);
    RefertoDTO getRefertoByNomeFile(String nomeFile);
    List<RefertoDTO> getRefertiByTipoEsame(TipoEsame tipoEsame);
    List<RefertoDTO> getRefertiByAutoreEmail(String autoreEmail);
    RefertoDTO getRefertoById(int id);
    List<RefertoDTO> getAllReferti();
}
