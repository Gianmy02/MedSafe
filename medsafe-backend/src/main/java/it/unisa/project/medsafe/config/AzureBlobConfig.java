package it.unisa.project.medsafe.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configurazione Azure Blob Storage con architettura Secretless.
 * Usa DefaultAzureCredential che:
 * - Su Azure App Service: usa Managed Identity
 * - In locale: usa Azure CLI (az login)
 * 
 * NON sono necessarie connection-string o chiavi!
 */
@Configuration
@Profile({ "azure" })
public class AzureBlobConfig {

    @Value("${spring.cloud.azure.storage.blob.endpoint}")
    private String storageEndpoint;

    @Value("${azure.storage.container-name:upload-dir}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }
        return containerClient;
    }
}
