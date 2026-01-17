package at.fhv.simplyevents.billing.application.usecase;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.domain.service.InvoiceNumberGenerator;
import at.fhv.simplyevents.billing.infrastructure.hash.InvoiceHashGenerator;
import at.fhv.simplyevents.billing.infrastructure.pdf.InvoicePdfGenerator;
import at.fhv.simplyevents.billing.infrastructure.persistence.mapper.InvoiceMapper;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Finalize an invoice.
 * Orchestrates:
 * 1. Invoice number generation (with locking)
 * 2. Hash computation
 * 3. PDF generation
 * 4. Domain finalization
 * 5. Persistence
 */
@Service
public class FinalizeInvoiceUseCase {

    private final InvoiceJpaRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceNumberGenerator numberGenerator;
    private final InvoiceHashGenerator hashGenerator;
    private final InvoicePdfGenerator pdfGenerator;

    public FinalizeInvoiceUseCase(
        InvoiceJpaRepository invoiceRepository,
        InvoiceMapper invoiceMapper,
        InvoiceNumberGenerator numberGenerator,
        InvoiceHashGenerator hashGenerator,
        InvoicePdfGenerator pdfGenerator
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.numberGenerator = numberGenerator;
        this.hashGenerator = hashGenerator;
        this.pdfGenerator = pdfGenerator;
    }

    @Transactional
    public Invoice execute(Long invoiceId) {
        var entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceException("Invoice not found: " + invoiceId));

        Invoice invoice = invoiceMapper.toDomain(entity);

        // Step 1: Generate unique invoice number (with pessimistic locking)
        var invoiceNumber = numberGenerator.generateNext();

        // Step 2: Compute hash over snapshot
        var hash = hashGenerator.generateHash(invoice);

        // Step 3: Generate PDF
        var pdfPath = pdfGenerator.generatePdf(invoice);

        // Step 4: Finalize invoice in domain (validates invariants, sets status to FINAL)
        invoice.finalize(invoiceNumber, hash, pdfPath);

        // Step 5: Persist
        var finalEntity = invoiceMapper.toEntity(invoice);
        var savedEntity = invoiceRepository.save(finalEntity);

        return invoiceMapper.toDomain(savedEntity);
    }
}
