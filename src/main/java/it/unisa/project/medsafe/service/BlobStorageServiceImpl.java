package it.unisa.project.medsafe.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class BlobStorageServiceImpl implements BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageServiceImpl(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        String uniqueFileName = generateUniqueFileName(fileName);

        BlobClient blobClient = containerClient.getBlobClient("immagini/" + uniqueFileName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        log.info("File caricato: {}", uniqueFileName);
        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadPdf(ByteArrayInputStream pdfStream, String fileName) {
        String uniqueFileName = generateUniqueFileName(fileName + ".pdf");

        BlobClient blobClient = containerClient.getBlobClient("pdf/" + uniqueFileName);
        blobClient.upload(pdfStream, pdfStream.available(), true);

        log.info("PDF caricato: {}", uniqueFileName);
        return blobClient.getBlobUrl();
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            if (blobClient.exists()) {
                blobClient.delete();
                log.info("File eliminato: {}", fileName);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Errore eliminazione file {}: {}", fileName, e.getMessage());
            return false;
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.getBlobUrl();
    }

    @Override
    public byte[] downloadFile(String blobPath) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobPath);
            if (blobClient.exists()) {
                return blobClient.downloadContent().toBytes();
            }
            log.warn("File non trovato: {}", blobPath);
            return new byte[0];
        } catch (Exception e) {
            log.error("Errore download file {}: {}", blobPath, e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Genera un nome file univoco per evitare sovrascritture
     */
    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return uuid + "_" + originalFileName;
    }
}
