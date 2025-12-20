package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.CheckedInParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckedInParticipantRepository extends JpaRepository<CheckedInParticipant, Long> {
    List<CheckedInParticipant> findByEventEventId(Long eventId);
    List<CheckedInParticipant> findByBookingId(Long bookingId);
    void deleteByBookingId(Long bookingId);
}
