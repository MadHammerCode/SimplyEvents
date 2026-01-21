package at.fhv.simplyevents.billing.application.usecase;

import at.fhv.simplyevents.billing.application.dto.InvoiceDto;
import at.fhv.simplyevents.billing.application.dto.InvoiceLineDto;
import at.fhv.simplyevents.billing.application.dto.InvoiceShareDto;
import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceJpaEntity;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;


@Service
public class GetInvoiceUseCase {

    private final InvoiceJpaRepository invoiceRepository;

    public GetInvoiceUseCase(InvoiceJpaRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public InvoiceDto execute(Long invoiceId) {
        var entity = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceException("Invoice not found: " + invoiceId));

        return mapToDto(entity);
    }

    private InvoiceDto mapToDto(InvoiceJpaEntity entity) {
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

        dto.setLines(
            entity.getLines().stream()
                .map(line -> new InvoiceLineDto(
                    line.getLineUuid(),
                    line.getDescription(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getLineTotal()
                ))
                .collect(Collectors.toList())
        );

        dto.setShares(
            entity.getShares().stream()
                .map(share -> new InvoiceShareDto(
                    share.getShareUuid(),
                    share.getUserId(),
                    share.getAllocatedAmount()
                ))
                .collect(Collectors.toList())
        );

        return dto;
    }
}
