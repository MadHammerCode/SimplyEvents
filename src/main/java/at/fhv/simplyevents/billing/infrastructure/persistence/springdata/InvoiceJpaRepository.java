package at.fhv.simplyevents.billing.infrastructure.persistence.springdata;

import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceJpaEntity;
import at.fhv.simplyevents.billing.domain.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, Long> {


    Optional<InvoiceJpaEntity> findByInvoiceNumber(String invoiceNumber);

    List<InvoiceJpaEntity> findByVendorId(Long vendorId);

    List<InvoiceJpaEntity> findByEventId(Long eventId);

    List<InvoiceJpaEntity> findByStatus(InvoiceStatus status);

    List<InvoiceJpaEntity> findByVendorIdAndStatus(Long vendorId, InvoiceStatus status);
}
