package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Participant;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepositoryPort {
    Participant save(Participant participant);
    void deleteByBookingId(Long bookingId);
    List<Participant> findByEventId(Long eventId);
    List<Participant> findByBookingId(Long bookingId);
    Optional<Participant> findById(Long id);
    long countByBookingId(Long bookingId);
    long countByBookingIdAndCheckedInTrue(Long bookingId);
}
