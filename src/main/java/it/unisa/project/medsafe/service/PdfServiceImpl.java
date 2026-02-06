package it.unisa.project.medsafe.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import it.unisa.project.medsafe.dto.RefertoDTO;
import it.unisa.project.medsafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final UserRepository userRepository;

    @Override
    public ByteArrayInputStream generaPdf(RefertoDTO dto) throws IOException {

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // === FONTS ===
            Font fontLogo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(0, 128, 128));
            Font fontTitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontLabelBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font fontSottotitolo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontNormale = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fontPiccolo = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontFirmaCorsivo = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14, new Color(0, 0, 139)); // Corsivo blu scuro

            // === 1. INTESTAZIONE CON LOGO A SINISTRA E TITOLO CENTRATO ===
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.5f, 3, 1.5f});

            // Logo a sinistra (immagine grande)
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            try {
                // Carica il logo dalla cartella resources/static/
                Image logoImage = Image.getInstance(getClass().getClassLoader().getResource("static/logo.png"));
                logoImage.scaleToFit(120, 120); // Ridimensiona a max 120x120 pixel
                logoCell.addElement(logoImage);
            } catch (Exception e) {
                // Se il logo non esiste, usa il testo di fallback
                Paragraph logoText = new Paragraph("MS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 48, new Color(0, 128, 0)));
                logoCell.addElement(logoText);
            }
            headerTable.addCell(logoCell);

            // Titolo centrato
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph logo = new Paragraph("MedSafe", fontLogo);
            logo.setAlignment(Element.ALIGN_CENTER);
            titleCell.addElement(logo);

            Paragraph titolo = new Paragraph("Piattaforma Online", fontTitolo);
            titolo.setAlignment(Element.ALIGN_CENTER);
            titleCell.addElement(titolo);

            headerTable.addCell(titleCell);

            // Info struttura a destra
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Font fontInfoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);

            Paragraph infoTitle = new Paragraph("Centro Diagnostico", fontInfoBold);
            infoTitle.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(infoTitle);

            Paragraph infoAddr = new Paragraph("Via della Salute, 123", fontInfo);
            infoAddr.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(infoAddr);

            Paragraph infoCity = new Paragraph("84100 Salerno (SA)", fontInfo);
            infoCity.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(infoCity);

            Paragraph infoTel = new Paragraph("Tel: 089 123456", fontInfo);
            infoTel.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(infoTel);

            headerTable.addCell(infoCell);

            document.add(headerTable);

            document.add(new Paragraph(" "));

            // === 2. DATI PAZIENTE (etichette in grassetto, valori normali) ===
            Paragraph pazientePara = new Paragraph();
            pazientePara.add(new Chunk("Paziente: ", fontLabelBold));
            pazientePara.add(new Chunk(dto.getNomePaziente() != null ? dto.getNomePaziente() : "", fontValue));
            document.add(pazientePara);
            document.add(new Paragraph(" "));

            Paragraph tipoEsamePara = new Paragraph();
            tipoEsamePara.add(new Chunk("Tipo d'esame: ", fontLabelBold));
            tipoEsamePara.add(new Chunk(dto.getTipoEsame() != null ? dto.getTipoEsame().getDescrizione() : "", fontValue));
            document.add(tipoEsamePara);
            document.add(new Paragraph(" "));

            Paragraph valutazionePara = new Paragraph();
            valutazionePara.add(new Chunk("Valutazione: ", fontLabelBold));
            valutazionePara.add(new Chunk(dto.getTestoReferto() != null ? dto.getTestoReferto() : "", fontValue));
            document.add(valutazionePara);
            document.add(new Paragraph(" "));

            // Linea separatrice
            LineSeparator linea = new LineSeparator();
            linea.setLineColor(Color.LIGHT_GRAY);
            document.add(new Chunk(linea));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // === 3. SEZIONE REFERTO (CONCLUSIONI) ===
            Paragraph titoloReferto = new Paragraph("REFERTO", fontSottotitolo);
            titoloReferto.setAlignment(Element.ALIGN_CENTER);
            titoloReferto.setSpacingAfter(15);
            document.add(titoloReferto);

            // Testo delle conclusioni
            String conclusioniTesto = dto.getConclusioni() != null ? dto.getConclusioni() : "";
            if (!conclusioniTesto.isEmpty()) {
                Paragraph rigaTesto = new Paragraph(conclusioniTesto, fontNormale);
                rigaTesto.setSpacingAfter(10);
                document.add(rigaTesto);
            }

            // Linee vuote per spazio firma
            document.add(new Paragraph(" "));
            document.add(new Chunk(linea));
            document.add(new Paragraph(" "));
            document.add(new Chunk(linea));
            document.add(new Paragraph(" "));
            document.add(new Chunk(linea));
            document.add(new Paragraph(" "));
            document.add(new Chunk(linea));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // === 4. FOOTER - FIRMA MEDICO ===
            PdfPTable tabellaFirma = new PdfPTable(2);
            tabellaFirma.setWidthPercentage(100);
            tabellaFirma.setWidths(new float[]{1, 1});

            // Colonna sinistra - Data
            String dataFormattata = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            PdfPCell cellData = new PdfPCell();
            cellData.setBorder(Rectangle.NO_BORDER);
            cellData.addElement(new Paragraph("Data refertazione: " + dataFormattata, fontPiccolo));
            tabellaFirma.addCell(cellData);

            // Colonna destra - Firma
            PdfPCell cellFirma = new PdfPCell();
            cellFirma.setBorder(Rectangle.NO_BORDER);
            cellFirma.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph medicoLabel = new Paragraph("Il Medico referente", fontNormale);
            medicoLabel.setAlignment(Element.ALIGN_RIGHT);
            cellFirma.addElement(medicoLabel);

            // Recupera il nome completo del medico dal database
            String nomeMedico = "[Nome non disponibile]";
            if (dto.getAutoreEmail() != null) {
                var userOpt = userRepository.findByEmail(dto.getAutoreEmail());
                if (userOpt.isPresent() && userOpt.get().getFullName() != null && !userOpt.get().getFullName().isEmpty()) {
                    nomeMedico = userOpt.get().getFullName();
                    // Aggiungi "Dott." se non presente
                    if (!nomeMedico.toLowerCase().startsWith("dott") && !nomeMedico.toLowerCase().startsWith("dr")) {
                        nomeMedico = "Dott. " + nomeMedico;
                    }
                }
            }

            // Firma digitale in corsivo
            Paragraph firmaDigitale = new Paragraph(nomeMedico, fontFirmaCorsivo);
            firmaDigitale.setAlignment(Element.ALIGN_RIGHT);
            firmaDigitale.setSpacingBefore(5);
            cellFirma.addElement(firmaDigitale);

            tabellaFirma.addCell(cellFirma);

            document.add(tabellaFirma);

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Errore durante la creazione del PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
