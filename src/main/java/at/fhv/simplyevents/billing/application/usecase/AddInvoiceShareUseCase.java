package at.fhv.simplyevents.billing.application.usecase;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.Invoice;
import at.fhv.simplyevents.billing.domain.model.Money;
import at.fhv.simplyevents.billing.domain.model.Percentage;
import at.fhv.simplyevents.billing.infrastructure.persistence.mapper.InvoiceMapper;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
public class AddInvoiceShareUseCase {

    private final InvoiceJpaRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public AddInvoiceShareUseCase(InvoiceJpaRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }


    @Transactional
    public Invoice executeByAmount(Long invoiceId, Long userId, BigDecimal amount) {
        var entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceException("Invoice not found: " + invoiceId));

        Invoice invoice = invoiceMapper.toDomain(entity);
        Money money = new Money(amount);
        invoice.allocateShareByAmount(userId, money);

        var updatedEntity = invoiceMapper.toEntity(invoice);
        var savedEntity = invoiceRepository.save(updatedEntity);

        return invoiceMapper.toDomain(savedEntity);
    }


    @Transactional
    public Invoice executeByPercentage(Long invoiceId, Long userId, BigDecimal percentage) {
        var entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceException("Invoice not found: " + invoiceId));

        Invoice invoice = invoiceMapper.toDomain(entity);
        Percentage pct = new Percentage(percentage);
        invoice.allocateShareByPercentage(userId, pct);

        var updatedEntity = invoiceMapper.toEntity(invoice);
        var savedEntity = invoiceRepository.save(updatedEntity);

        return invoiceMapper.toDomain(savedEntity);
    }
}
