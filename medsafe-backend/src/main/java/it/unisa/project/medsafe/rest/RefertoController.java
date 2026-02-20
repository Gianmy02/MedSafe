package it.unisa.project.medsafe.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entity.TipoEsame;
import it.unisa.project.medsafe.service.BlobStorageService;
import it.unisa.project.medsafe.service.RefertoService;
import it.unisa.project.medsafe.utils.JwtHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("referti")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Referti", description = "API per la gestione dei referti medici")
public class RefertoController {

    @Autowired
    private RefertoService refertoService;

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private JwtHelper jwtHelper;

    @Operation(summary = "Carica nuovo referto", description = "Carica un referto medico con immagine diagnostica")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<?> addReferto(
            @Parameter(description = "Nome del paziente", required = true) @RequestParam String nomePaziente,

            @Parameter(description = "Codice Fiscale (16 caratteri)", required = true) @RequestParam String codiceFiscale,

            @Parameter(description = "Tipo di esame (TAC, Radiografia, Ecografia, Risonanza, Esami_Laboratorio)", required = true) @RequestParam TipoEsame tipoEsame,

            @Parameter(description = "Testo del referto medico", required = true) @RequestParam(required = false) String testoReferto,

            @Parameter(description = "Conclusioni del referto", required = true) @RequestParam(required = false) String conclusioni,

            @Parameter(description = "Email del medico refertante (opzionale, estratta automaticamente dal JWT se presente)", required = false) @RequestParam(required = false) String autoreEmail,

            @Parameter(description = "Nome del file referto da salvare", required = true) @RequestParam String nomeFile,

            @Parameter(description = "File immagine diagnostica (PDF, JPG, PNG)", required = true) @RequestPart("file") MultipartFile file) {

        // Validazione estensione file
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.badRequest().body("Nome file non valido");
        }

        String extension = filename.toLowerCase();
        if (!extension.endsWith(".png") && !extension.endsWith(".jpg")
                && !extension.endsWith(".jpeg") && !extension.endsWith(".pdf")) {
            return ResponseEntity.badRequest()
                    .body("Formato file non supportato. Estensioni consentite: PNG, JPG, JPEG, PDF");
        }

        // Estrai email automaticamente dal JWT (Azure AD) se non fornita
        if (autoreEmail == null || autoreEmail.isBlank()) {
            autoreEmail = jwtHelper.getCurrentUserEmail();
            if (autoreEmail == null) {
                log.warn("⚠️  Nessuna email trovata nel JWT, usando email di default per testing");
            }
        }

        log.info("=== INIZIO addReferto ===");
        log.info("Tipo Esame: {}", tipoEsame);
        log.info("File size: {} bytes", file.getSize());

        RefertoDTO dto = RefertoDTO.builder()
                .nomePaziente(nomePaziente)
                .codiceFiscale(codiceFiscale.toUpperCase())
                .tipoEsame(tipoEsame)
                .testoReferto(testoReferto)
                .conclusioni(conclusioni)
                .autoreEmail(autoreEmail)
                .nomeFile(nomeFile)
                .build();

        try {
            refertoService.addReferto(dto, file);
            log.info("=== FINE addReferto - SUCCESSO ===");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("❌ ERRORE CARICAMENTO REFERTO: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore dettagliato durante il caricamento: " + e.getMessage());
        }
    }

    @Operation(summary = "Modifica referto", description = "Modifica un referto esistente (supporta upload nuova immagine)")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> editReferto(
            @RequestPart("referto") @jakarta.validation.Valid RefertoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("=== RICHIESTA MODIFICA REFERTO ===");
        log.info("ID Referto: {}", dto.getId());
        log.info("File presente: {}", (file != null && !file.isEmpty()));

        if (refertoService.editReferto(dto, file)) {
            log.info("=== MODIFICA COMPLETATA CON SUCCESSO ===");
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }

        log.warn("=== MODIFICA FALLITA: REFERTO NON TROVATO O ERRORE ===");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Elimina referto", description = "Elimina un referto per ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> removeReferto(@PathVariable int id) {
        if (refertoService.removeReferto(id))
            return ResponseEntity.status(HttpStatus.OK).body(null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Cerca per Codice Fiscale", description = "Restituisce tutti i referti di un paziente")
    @GetMapping("codiceFiscale")
    public ResponseEntity<List<RefertoDTO>> getRefertoByCodiceFiscale(
            @Parameter(description = "Codice Fiscale del paziente") @RequestParam String value) {
        return ResponseEntity.ok(refertoService.getRefertoByCodiceFiscale(value));
    }

    @Operation(summary = "Cerca per Nome File", description = "Restituisce un referto specifico")
    @GetMapping("nomeFile")
    public ResponseEntity<RefertoDTO> getRefertoByNomeFile(
            @Parameter(description = "Nome del file") @RequestParam String value) {
        RefertoDTO referto = refertoService.getRefertoByNomeFile(value);
        if (referto != null)
            return ResponseEntity.ok(referto);
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Cerca per Tipo Esame", description = "Restituisce tutti i referti di un tipo esame")
    @GetMapping("tipoEsame")
    public ResponseEntity<List<RefertoDTO>> getRefertiByTipoEsame(
            @Parameter(description = "Tipo di esame (TAC, Radiografia, Ecografia, Risonanza, Esami_Laboratorio)") @RequestParam TipoEsame value) {
        return ResponseEntity.ok(refertoService.getRefertiByTipoEsame(value));
    }

    @Operation(summary = "Cerca per Email Autore", description = "Restituisce tutti i referti di un medico")
    @GetMapping("email")
    public ResponseEntity<List<RefertoDTO>> getRefertiByAutoreEmail(
            @Parameter(description = "Email del medico") @RequestParam String value) {
        return ResponseEntity.ok(refertoService.getRefertiByAutoreEmail(value));
    }

    @Operation(summary = "Ottieni tutti i referti", description = "Restituisce tutti i referti presenti nel sistema")
    @GetMapping
    public ResponseEntity<List<RefertoDTO>> getAllReferti() {
        return ResponseEntity.ok(refertoService.getAllReferti());
    }

    @Operation(summary = "Scarica PDF", description = "Scarica il PDF generato di un referto")
    @GetMapping("download/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable int id) {
        RefertoDTO referto = refertoService.getRefertoById(id);
        if (referto == null || referto.getUrlPdfGenerato() == null) {
            return ResponseEntity.notFound().build();
        }

        // Estrai il path del blob dall'URL
        String url = referto.getUrlPdfGenerato();
        log.info("Download PDF richiesto per referto ID: {}", id);

        String containerName = "upload-dir";
        String blobPath = null;

        // Strategia 1: Cerca "/upload-dir/"
        int index = url.indexOf("/" + containerName + "/");
        if (index != -1) {
            blobPath = url.substring(index + containerName.length() + 2);
        } else {
            // Strategia 2: Controlla se inizia con "upload-dir/"
            if (url.startsWith(containerName + "/")) {
                blobPath = url.substring(containerName.length() + 1);
            }
        }

        if (blobPath == null) {
            log.warn("Impossibile estrarre blobPath dall'URL (fallback al path intero): {}", url);
            blobPath = url; // Fallback estremo
        }

        blobPath = java.net.URLDecoder.decode(blobPath, java.nio.charset.StandardCharsets.UTF_8);

        byte[] content = blobStorageService.downloadFile(blobPath);
        if (content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Rimuovi l'estensione originale dal nome file
        String nomeFile = referto.getNomeFile();
        if (nomeFile.contains(".")) {
            nomeFile = nomeFile.substring(0, nomeFile.lastIndexOf("."));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeFile + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @Operation(summary = "Scarica Immagine", description = "Scarica l'immagine diagnostica di un referto")
    @GetMapping("download/immagine/{id}")
    public ResponseEntity<byte[]> downloadImmagine(@PathVariable int id) {
        RefertoDTO referto = refertoService.getRefertoById(id);
        if (referto == null || referto.getFileUrlImmagine() == null) {
            return ResponseEntity.notFound().build();
        }

        // Estrai il path del blob dall'URL
        String url = referto.getFileUrlImmagine();
        log.info("Download Immagine richiesto per referto ID: {}", id);

        String containerName = "upload-dir";
        String blobPath = null;

        // Strategia 1: Cerca "/upload-dir/"
        int index = url.indexOf("/" + containerName + "/");
        if (index != -1) {
            blobPath = url.substring(index + containerName.length() + 2);
        } else {
            // Strategia 2: Controlla se inizia con "upload-dir/"
            if (url.startsWith(containerName + "/")) {
                blobPath = url.substring(containerName.length() + 1);
            }
        }

        if (blobPath == null) {
            log.warn("Impossibile estrarre blobPath dall'URL immagine (fallback al path intero): {}", url);
            blobPath = url; // Fallback estremo
        }

        blobPath = java.net.URLDecoder.decode(blobPath, java.nio.charset.StandardCharsets.UTF_8);

        byte[] content = blobStorageService.downloadFile(blobPath);
        if (content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Estrai l'estensione originale dall'URL del file caricato
        String estensione = "";
        String urlLower = url.toLowerCase();
        if (urlLower.contains(".")) {
            estensione = url.substring(url.lastIndexOf("."));
        }

        // Costruisci il nome file con l'estensione originale
        String nomeFile = referto.getNomeFile();
        // Rimuovi eventuale estensione esistente dal nome
        if (nomeFile.contains(".")) {
            nomeFile = nomeFile.substring(0, nomeFile.lastIndexOf("."));
        }
        // Aggiungi l'estensione originale
        String nomeFileCompleto = nomeFile + estensione;

        // Rileva il MediaType in base all'estensione
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String estensioneMinuscola = estensione.toLowerCase();
        if (estensioneMinuscola.equals(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (estensioneMinuscola.equals(".jpg") || estensioneMinuscola.equals(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (estensioneMinuscola.equals(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeFileCompleto + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
