package at.fhv.simplyevents.billing.interfaces.rest;

import at.fhv.simplyevents.billing.application.dto.InvoiceDto;
import at.fhv.simplyevents.billing.application.usecase.*;
import at.fhv.simplyevents.billing.domain.exception.ImmutableInvoiceException;
import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final CreateInvoiceDraftUseCase createDraftUseCase;
    private final AddInvoiceLineUseCase addLineUseCase;
    private final AddInvoiceShareUseCase addShareUseCase;
    private final FinalizeInvoiceUseCase finalizeUseCase;
    private final GetInvoiceUseCase getInvoiceUseCase;
    private final InvoiceJpaRepository invoiceRepository;

    public InvoiceController(
        CreateInvoiceDraftUseCase createDraftUseCase,
        AddInvoiceLineUseCase addLineUseCase,
        AddInvoiceShareUseCase addShareUseCase,
        FinalizeInvoiceUseCase finalizeUseCase,
        GetInvoiceUseCase getInvoiceUseCase,
        InvoiceJpaRepository invoiceRepository
    ) {
        this.createDraftUseCase = createDraftUseCase;
        this.addLineUseCase = addLineUseCase;
        this.addShareUseCase = addShareUseCase;
        this.finalizeUseCase = finalizeUseCase;
        this.getInvoiceUseCase = getInvoiceUseCase;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllInvoices() {
        try {
            List<?> invoices = invoiceRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .toList();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving invoices: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDraft(
        @RequestParam Long eventId,
        @RequestParam Long vendorId
    ) {
        try {
            Invoice invoice = createDraftUseCase.execute(eventId, vendorId);
            InvoiceDto dto = mapToDto(invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (InvoiceValidationException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating invoice: " + e.getMessage());
        }
    }

    @PostMapping("/{invoiceId}/lines")
    public ResponseEntity<?> addLine(
        @PathVariable Long invoiceId,
        @RequestParam String description,
        @RequestParam int quantity,
        @RequestParam BigDecimal unitPrice
    ) {
        try {
            Invoice invoice = addLineUseCase.execute(invoiceId, description, quantity, unitPrice);
            InvoiceDto dto = mapToDto(invoice);
            return ResponseEntity.ok(dto);
        } catch (ImmutableInvoiceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot modify invoice: " + e.getMessage());
        } catch (InvoiceValidationException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding line: " + e.getMessage());
        }
    }

    @PostMapping("/{invoiceId}/shares")
    public ResponseEntity<?> addShare(
        @PathVariable Long invoiceId,
        @RequestParam Long userId,
        @RequestParam(required = false) BigDecimal amount,
        @RequestParam(required = false) BigDecimal percentage
    ) {
        try {
            Invoice invoice;
            if (amount != null) {
                invoice = addShareUseCase.executeByAmount(invoiceId, userId, amount);
            } else if (percentage != null) {
                invoice = addShareUseCase.executeByPercentage(invoiceId, userId, percentage);
            } else {
                return ResponseEntity.badRequest().body("Either amount or percentage must be provided");
            }

            InvoiceDto dto = mapToDto(invoice);
            return ResponseEntity.ok(dto);
        } catch (ImmutableInvoiceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot modify invoice: " + e.getMessage());
        } catch (InvoiceValidationException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding share: " + e.getMessage());
        }
    }

    @PostMapping("/{invoiceId}/finalize")
    public ResponseEntity<?> finalize(@PathVariable Long invoiceId) {
        try {
            Invoice invoice = finalizeUseCase.execute(invoiceId);
            InvoiceDto dto = mapToDto(invoice);
            return ResponseEntity.ok(dto);
        } catch (ImmutableInvoiceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot finalize invoice: " + e.getMessage());
        } catch (InvoiceValidationException e) {
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error finalizing invoice: " + e.getMessage());
        }
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoice(@PathVariable Long invoiceId) {
        try {
            InvoiceDto dto = getInvoiceUseCase.execute(invoiceId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<?> downloadPdf(@PathVariable Long invoiceId) {
        try {
            InvoiceDto invoice = getInvoiceUseCase.execute(invoiceId);
            if (invoice.getPdfPath() == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfContent = Files.readAllBytes(Paths.get(invoice.getPdfPath()));
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=invoice_" + invoiceId + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading PDF: " + e.getMessage());
        }
    }

    private InvoiceDto mapToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(invoice.getId());
        dto.setEventId(invoice.getEventId());
        dto.setVendorId(invoice.getVendorId());
        dto.setStatus(invoice.getStatus());
        if (invoice.getInvoiceNumber() != null) {
            dto.setInvoiceNumber(invoice.getInvoiceNumber().format());
        }
        dto.setTotal(invoice.getTotal().getAmount());
        dto.setCurrency(invoice.getTotal().getCurrency());
        dto.setHash(invoice.getHash());
        dto.setPdfPath(invoice.getPdfPath());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        return dto;
    }

    private InvoiceDto mapEntityToDto(at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceJpaEntity entity) {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(entity.getInvoiceId());
        dto.setEventId(entity.getEventId());
        dto.setVendorId(entity.getVendorId());
        dto.setStatus(entity.getStatus());
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setTotal(entity.getTotal());
        dto.setCurrency("EUR");
        dto.setHash(entity.getHash());
        dto.setPdfPath(entity.getPdfPath());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Map lines
        if (entity.getLines() != null && !entity.getLines().isEmpty()) {
            dto.setLines(entity.getLines().stream()
                .map(line -> new at.fhv.simplyevents.billing.application.dto.InvoiceLineDto(
                    line.getLineUuid(),
                    line.getDescription(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getLineTotal()
                ))
                .toList());
        }

        // Map shares
        if (entity.getShares() != null && !entity.getShares().isEmpty()) {
            dto.setShares(entity.getShares().stream()
                .map(share -> new at.fhv.simplyevents.billing.application.dto.InvoiceShareDto(
                    share.getShareUuid(),
                    share.getUserId(),
                    share.getAllocatedAmount()
                ))
                .toList());
        }

        return dto;
    }
}
