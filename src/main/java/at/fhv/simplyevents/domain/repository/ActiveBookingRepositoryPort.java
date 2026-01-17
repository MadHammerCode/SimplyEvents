package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.ActiveBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActiveBookingRepositoryPort {
    ActiveBooking save(ActiveBooking booking);
    Optional<ActiveBooking> findById(Long id);
    Optional<ActiveBooking> findByBookingNumber(String bookingNumber);
    int sumParticipantsByEventId(Long eventId);
    int sumParticipantsByEventIdExcludingBooking(Long eventId, Long bookingId);
    void delete(ActiveBooking booking);
    List<ActiveBooking> findByEventId(Long eventId);
    int sumParticipantsByEventIdAndDate(Long eventId, LocalDate date);
}
