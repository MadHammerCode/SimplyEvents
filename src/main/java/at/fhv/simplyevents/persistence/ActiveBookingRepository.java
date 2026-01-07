package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.ActiveBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/**
 * Deprecated legacy repository using domain model; replaced by ActiveBookingJpaRepository.
 */
@Deprecated
public interface ActiveBookingRepository extends JpaRepository<ActiveBooking, Long> {

    Optional<ActiveBooking> findByBookingNumber(String bookingNumber);

    @Query("""
           select coalesce(sum(b.numParticipants), 0)
           from ActiveBooking b
           where b.event.eventId = :eventId
           """)
    int sumParticipantsByEventId(@Param("eventId") Long eventId);

    @Query("""
           select coalesce(sum(b.numParticipants), 0)
           from ActiveBooking b
           where b.event.eventId = :eventId and b.id <> :bookingId
           """)
    int sumParticipantsByEventIdExcludingBooking(@Param("eventId") Long eventId, @Param("bookingId") Long bookingId);

    List<ActiveBooking> findByEventEventId(Long eventId);
}
