package at.fhv.simplyevents.billing.infrastructure.persistence.mapper;

import at.fhv.simplyevents.billing.domain.model.*;
import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceJpaEntity;
import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceLineJpaEntity;
import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceShareJpaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class InvoiceMapper {


    public Invoice toDomain(InvoiceJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Invoice invoice = new Invoice(
            entity.getInvoiceId(),
            entity.getEventId(),
            entity.getVendorId(),
            entity.getCreatedAt()
        );

        // Set status using public setter
        invoice.setStatus(entity.getStatus());


        if (entity.getInvoiceNumber() != null) {
            String[] parts = entity.getInvoiceNumber().split("-");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                long sequence = Long.parseLong(parts[1]);
                invoice.setInvoiceNumber(new InvoiceNumber(year, sequence));
            }
        }

        invoice.setHash(entity.getHash());
        invoice.setPdfPath(entity.getPdfPath());

        // Map lines
        if (entity.getLines() != null && !entity.getLines().isEmpty()) {
            invoice.setLines(
                entity.getLines().stream()
                    .map(this::toDomainLine)
                    .collect(Collectors.toList())
            );
        }

        // Map shares
        if (entity.getShares() != null && !entity.getShares().isEmpty()) {
            invoice.setShares(
                entity.getShares().stream()
                    .map(this::toDomainShare)
                    .collect(Collectors.toList())
            );
        }

        // Set timestamp
        invoice.setUpdatedAt(entity.getUpdatedAt());

        return invoice;
    }


    public InvoiceJpaEntity toEntity(Invoice domain) {
        if (domain == null) {
            return null;
        }


        InvoiceJpaEntity entity = new InvoiceJpaEntity(domain.getEventId(), domain.getVendorId());


        if (domain.getId() != null) {
            entity.setInvoiceId(domain.getId());
        }
        entity.setStatus(domain.getStatus());

        if (domain.getInvoiceNumber() != null) {
            entity.setInvoiceNumber(domain.getInvoiceNumber().format());
        }

        entity.setTotal(domain.getTotal().getAmount());
        entity.setHash(domain.getHash());
        entity.setPdfPath(domain.getPdfPath());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // Map lines
        if (domain.getLines() != null && !domain.getLines().isEmpty()) {
            entity.setLines(
                domain.getLines().stream()
                    .map(line -> toEntityLine(line, domain.getId()))
                    .collect(Collectors.toList())
            );
        }

        // Map shares
        if (domain.getShares() != null && !domain.getShares().isEmpty()) {
            entity.setShares(
                domain.getShares().stream()
                    .map(share -> toEntityShare(share, domain.getId()))
                    .collect(Collectors.toList())
            );
        }

        return entity;
    }

    private InvoiceLine toDomainLine(InvoiceLineJpaEntity entity) {
        Money unitPrice = new Money(entity.getUnitPrice());
        return new InvoiceLine(
            entity.getDescription(),
            entity.getQuantity(),
            unitPrice
        );
    }

    private InvoiceLineJpaEntity toEntityLine(InvoiceLine domain, Long invoiceId) {
        return new InvoiceLineJpaEntity(
            domain.getId(),
            domain.getDescription(),
            domain.getQuantity(),
            domain.getUnitPrice().getAmount(),
            domain.getLineTotal().getAmount()
        );
    }

    private InvoiceShare toDomainShare(InvoiceShareJpaEntity entity) {
        Money allocatedAmount = new Money(entity.getAllocatedAmount());
        return new InvoiceShare(entity.getUserId(), allocatedAmount);
    }

    private InvoiceShareJpaEntity toEntityShare(InvoiceShare domain, Long invoiceId) {
        return new InvoiceShareJpaEntity(
            domain.getId(),
            domain.getUserId(),
            domain.getAllocatedAmount().getAmount()
        );
    }
}
