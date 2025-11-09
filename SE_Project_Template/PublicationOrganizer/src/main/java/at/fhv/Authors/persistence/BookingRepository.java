package at.fhv.Authors.persistence;

import at.fhv.Authors.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
