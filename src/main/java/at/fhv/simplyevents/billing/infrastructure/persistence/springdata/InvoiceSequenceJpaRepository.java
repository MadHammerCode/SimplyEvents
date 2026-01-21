package at.fhv.simplyevents.billing.infrastructure.persistence.springdata;

import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceSequenceJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceSequenceJpaRepository extends JpaRepository<InvoiceSequenceJpaEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequenceJpaEntity s WHERE s.year = :year")
    Optional<InvoiceSequenceJpaEntity> findByYearWithLock(@Param("year") Integer year);
}
