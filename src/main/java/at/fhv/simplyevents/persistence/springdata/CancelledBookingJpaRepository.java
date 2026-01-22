package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.CancelledBookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancelledBookingJpaRepository extends JpaRepository<CancelledBookingJpaEntity, Long> {
    List<CancelledBookingJpaEntity> findByEmail(String email);
}

