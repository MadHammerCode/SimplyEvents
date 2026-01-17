package at.fhv.simplyevents.billing.application.usecase;

import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.infrastructure.persistence.mapper.InvoiceMapper;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Create a new draft invoice.
 */
@Service
public class CreateInvoiceDraftUseCase {

    private final InvoiceJpaRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public CreateInvoiceDraftUseCase(InvoiceJpaRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional
    public Invoice execute(Long eventId, Long vendorId) {
        Invoice invoice = new Invoice(
            null, // Will be assigned by DB
            eventId,
            vendorId,
            java.time.LocalDateTime.now()
        );

        // Save to get ID
        var entity = invoiceMapper.toEntity(invoice);
        var savedEntity = invoiceRepository.save(entity);

        // Return persisted domain object
        return invoiceMapper.toDomain(savedEntity);
    }
}
