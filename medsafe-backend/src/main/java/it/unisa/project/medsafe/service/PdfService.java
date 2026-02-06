package it.unisa.project.medsafe.service;

import it.unisa.project.medsafe.dto.RefertoDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface PdfService {
    /**
     * Genera un PDF del referto medico
     * @param dto i dati del referto
     * @return stream del PDF generato
     */
    ByteArrayInputStream generaPdf(RefertoDTO dto) throws IOException;
}