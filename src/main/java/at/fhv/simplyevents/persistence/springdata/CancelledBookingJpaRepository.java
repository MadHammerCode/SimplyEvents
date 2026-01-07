package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.CancelledBookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelledBookingJpaRepository extends JpaRepository<CancelledBookingJpaEntity, Long> {
}

