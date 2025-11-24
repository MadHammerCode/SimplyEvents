package at.fhv.simplyevents.persistence;


import at.fhv.simplyevents.domain.model.CancelledBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelledBookingRepository extends JpaRepository<CancelledBooking, Long> {
}
