package at.fhv.simplyevents.billing.application.usecase;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.domain.model.Money;
import at.fhv.simplyevents.billing.infrastructure.persistence.mapper.InvoiceMapper;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Use Case: Add a line to an invoice.
 */
@Service
public class AddInvoiceLineUseCase {

    private final InvoiceJpaRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public AddInvoiceLineUseCase(InvoiceJpaRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional
    public Invoice execute(Long invoiceId, String description, int quantity, BigDecimal unitPrice) {
        var entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceException("Invoice not found: " + invoiceId));

        Invoice invoice = invoiceMapper.toDomain(entity);
        Money price = new Money(unitPrice);
        invoice.addLine(description, quantity, price);

        var updatedEntity = invoiceMapper.toEntity(invoice);
        var savedEntity = invoiceRepository.save(updatedEntity);

        return invoiceMapper.toDomain(savedEntity);
    }
}
