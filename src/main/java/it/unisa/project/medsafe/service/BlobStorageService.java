package it.unisa.project.medsafe.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Servizio per la gestione dei file su Azure Blob Storage.
 * Permette di caricare, scaricare e eliminare file dal cloud.
 */
public interface BlobStorageService {

    /**
     * Carica un file su Azure Blob Storage
     * @param file il file da caricare (immagine diagnostica)
     * @param fileName nome con cui salvare il file
     * @return URL pubblico del file caricato
     */
    String uploadFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Carica un PDF generato su Azure Blob Storage
     * @param pdfStream stream del PDF generato
     * @param fileName nome con cui salvare il PDF
     * @return URL pubblico del PDF caricato
     */
    String uploadPdf(ByteArrayInputStream pdfStream, String fileName) throws IOException;

    /**
     * Elimina un file da Azure Blob Storage
     * @param fileName nome del file da eliminare
     * @return true se eliminato con successo
     */
    boolean deleteFile(String fileName);

    /**
     * Ottiene l'URL di un file esistente
     * @param fileName nome del file
     * @return URL del file
     */
    String getFileUrl(String fileName);

    /**
     * Scarica un file da Azure Blob Storage
     * @param blobPath percorso del blob (es. "pdf/nomefile.pdf")
     * @return byte array del file
     */
    byte[] downloadFile(String blobPath);
}
