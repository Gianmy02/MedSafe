package it.unisa.project.medsafe.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.entinty.TipoEsame;
import it.unisa.project.medsafe.service.BlobStorageService;
import it.unisa.project.medsafe.service.RefertoService;
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

    @Operation(summary = "Carica nuovo referto", description = "Carica un referto medico con immagine diagnostica")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<?> addReferto(
            @Parameter(description = "Nome del paziente", required = true)
            @RequestParam String nomePaziente,

            @Parameter(description = "Codice Fiscale (16 caratteri)", required = true)
            @RequestParam String codiceFiscale,

            @Parameter(description = "Tipo di esame (TAC, RADIOGRAFIA, ECOGRAFIA, RISONANZA, ESAMI_LABORATORIO)", required = true)
            @RequestParam TipoEsame tipoEsame,

            @Parameter(description = "Testo del referto medico", required = true)
            @RequestParam(required = false) String testoReferto,

            @Parameter(description = "Conclusioni del referto", required = true)
            @RequestParam(required = false) String conclusioni,

            @Parameter(description = "Email del medico refertante", required = true)
            @RequestParam String autoreEmail,

            @Parameter(description = "Nome del file referto da salvare", required = true)
            @RequestParam String nomeFile,

            @Parameter(description = "File immagine diagnostica (PDF, JPG, PNG)", required = true)
            @RequestPart("file") MultipartFile file) {

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

        log.info("=== INIZIO addReferto ===");
        log.info("Paziente: {}", nomePaziente);
        log.info("Codice Fiscale: {}", codiceFiscale);
        log.info("Tipo Esame: {}", tipoEsame);
        log.info("Autore Email: {}", autoreEmail);
        log.info("Nome File: {}", nomeFile);
        log.info("File: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        RefertoDTO dto = RefertoDTO.builder()
                .nomePaziente(nomePaziente)
                .codiceFiscale(codiceFiscale.toUpperCase())
                .tipoEsame(tipoEsame)
                .testoReferto(testoReferto)
                .conclusioni(conclusioni)
                .autoreEmail(autoreEmail)
                .nomeFile(nomeFile)
                .build();

        refertoService.addReferto(dto, file);

        log.info("=== FINE addReferto - SUCCESSO ===");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Modifica referto", description = "Modifica un referto esistente")
    @PutMapping
    public ResponseEntity<Void> editReferto(@RequestBody RefertoDTO dto) {
        if (refertoService.editReferto(dto))
            return ResponseEntity.status(HttpStatus.OK).body(null);

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
            @Parameter(description = "Tipo di esame (TAC, RADIOGRAFIA, ECOGRAFIA, RISONANZA, ESAMI_LABORATORIO)") @RequestParam TipoEsame value) {
        return ResponseEntity.ok(refertoService.getRefertiByTipoEsame(value));
    }

    @Operation(summary = "Cerca per Email Autore", description = "Restituisce tutti i referti di un medico")
    @GetMapping("email")
    public ResponseEntity<List<RefertoDTO>> getRefertiByAutoreEmail(
            @Parameter(description = "Email del medico") @RequestParam String value) {
        return ResponseEntity.ok(refertoService.getRefertiByAutoreEmail(value));
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
        String blobPath = url.substring(url.indexOf("/referti/") + 9); // prende "pdf/nomefile.pdf"
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
        String blobPath = url.substring(url.indexOf("/referti/") + 9);
        blobPath = java.net.URLDecoder.decode(blobPath, java.nio.charset.StandardCharsets.UTF_8);

        byte[] content = blobStorageService.downloadFile(blobPath);
        if (content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Rileva il MediaType in base all'estensione del file
        String nomeFile = referto.getNomeFile().toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (nomeFile.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (nomeFile.endsWith(".jpg") || nomeFile.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (nomeFile.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + referto.getNomeFile() + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
