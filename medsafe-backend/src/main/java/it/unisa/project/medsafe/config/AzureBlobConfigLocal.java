package it.unisa.project.medsafe.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configurazione Azure Blob Storage per sviluppo locale con Azurite (Docker).
 * Usa la connection-string dell'emulatore Azurite.
 * 
 * Questa configurazione è SOLO per sviluppo locale, NON per produzione!
 */
@Configuration
@Profile({ "local", "docker", "test" })
public class AzureBlobConfigLocal {

    @Value("${azure.storage.connection-string:DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;}")
    private String connectionString;

    @Value("${azure.storage.container-name:upload-dir}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        try {
            if (!containerClient.exists()) {
                containerClient.create();
            }
        } catch (Exception e) {
            // In ambiente locale senza Azurite, ignora l'errore
        }
        return containerClient;
    }
}
