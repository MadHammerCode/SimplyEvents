package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.ActiveBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<ActiveBooking, Long> {

    Optional<ActiveBooking> findByBookingNumber(String bookingNumber);

    @Query("""
           select coalesce(sum(b.numParticipants), 0)
           from ActiveBooking b
           where b.event.eventId = :eventId
           """)
    int sumParticipantsByEventId(@Param("eventId") Long eventId);
}
