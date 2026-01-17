package at.fhv.simplyevents.billing.infrastructure.pdf;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.domain.model.InvoiceLine;
import at.fhv.simplyevents.billing.domain.model.InvoiceShare;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;


@Service
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Value("${billing.pdf-output-dir:billing/invoices}")
    private String pdfOutputDir;


    public String generatePdf(Invoice invoice) {
        try {

            Path outputPath = Paths.get(pdfOutputDir);
            Files.createDirectories(outputPath);


            String filename = String.format("invoice_%d_%s.pdf",
                invoice.getId(),
                System.currentTimeMillis());
            Path filePath = outputPath.resolve(filename);


            PdfWriter writer = new PdfWriter(filePath.toString());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);


            addTitle(document);
            addInvoiceInfo(document, invoice);
            addLineItems(document, invoice);
            addAllocations(document, invoice);
            addFooter(document, invoice);

            document.close();

            return filePath.toString();
        } catch (IOException e) {
            throw new InvoiceException("Failed to generate PDF for invoice " + invoice.getId(), e);
        }
    }

    private void addTitle(Document document) {
        Paragraph title = new Paragraph("RECHNUNG / INVOICE")
            .setFontSize(24)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        document.add(new Paragraph("\n")); // Spacing
    }

    private void addInvoiceInfo(Document document, Invoice invoice) {
        Paragraph infoSection = new Paragraph("RECHNUNGSINFORMATIONEN")
            .setFontSize(12)
            .setBold();
        document.add(infoSection);

        if (invoice.getInvoiceNumber() != null) {
            document.add(new Paragraph("Rechnungsnummer: " + invoice.getInvoiceNumber().format())
                .setFontSize(10));
        }

        document.add(new Paragraph("Erstellt: " + invoice.getCreatedAt().format(DATE_FORMATTER))
            .setFontSize(10));

        document.add(new Paragraph("Event ID: " + invoice.getEventId())
            .setFontSize(10));

        document.add(new Paragraph("Anbieter ID: " + invoice.getVendorId())
            .setFontSize(10));

        document.add(new Paragraph("Status: " + invoice.getStatus())
            .setFontSize(10));

        document.add(new Paragraph("\n")); // Spacing
    }

    private void addLineItems(Document document, Invoice invoice) {
        Paragraph itemsSection = new Paragraph("POSITIONEN")
            .setFontSize(12)
            .setBold();
        document.add(itemsSection);


        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 15, 20, 25}))
            .setWidth(UnitValue.createPercentValue(100));


        table.addHeaderCell("Beschreibung").setTextAlignment(TextAlignment.LEFT);
        table.addHeaderCell("Menge").setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell("Einzelpreis").setTextAlignment(TextAlignment.RIGHT);
        table.addHeaderCell("Gesamtpreis").setTextAlignment(TextAlignment.RIGHT);


        for (InvoiceLine line : invoice.getLines()) {
            table.addCell(truncate(line.getDescription(), 50))
                .setTextAlignment(TextAlignment.LEFT);
            table.addCell(String.valueOf(line.getQuantity()))
                .setTextAlignment(TextAlignment.CENTER);
            table.addCell("€ " + formatAmount(line.getUnitPrice().getAmount()))
                .setTextAlignment(TextAlignment.RIGHT);
            table.addCell("€ " + formatAmount(line.getLineTotal().getAmount()))
                .setTextAlignment(TextAlignment.RIGHT);
        }

        document.add(table);


        document.add(new Paragraph("\n")); // Spacing
        Paragraph total = new Paragraph("GESAMTBETRAG: € " + formatAmount(invoice.getTotal().getAmount()))
            .setFontSize(12)
            .setBold()
            .setTextAlignment(TextAlignment.RIGHT);
        document.add(total);

        document.add(new Paragraph("\n")); // Spacing
    }

    private void addAllocations(Document document, Invoice invoice) {
        if (invoice.getShares().isEmpty()) {
            return;
        }

        Paragraph allocSection = new Paragraph("VERTEILUNGEN / ZUWEISUNGEN")
            .setFontSize(12)
            .setBold();
        document.add(allocSection);

        for (InvoiceShare share : invoice.getShares()) {
            document.add(new Paragraph("Benutzer " + share.getUserId() + ": € " + formatAmount(share.getAllocatedAmount().getAmount()))
                .setFontSize(10));
        }

        document.add(new Paragraph("\n")); // Spacing
    }

    private void addFooter(Document document, Invoice invoice) {
        if (invoice.getHash() != null) {
            Paragraph footer = new Paragraph("Hash: " + truncate(invoice.getHash(), 80))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
            document.add(footer);
        }
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }
}
