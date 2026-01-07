package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.ParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantJpaRepository extends JpaRepository<ParticipantJpaEntity, Long> {
    void deleteByBookingId(Long bookingId);
    List<ParticipantJpaEntity> findByEventId(Long eventId);
    List<ParticipantJpaEntity> findByBookingId(Long bookingId);
    Optional<ParticipantJpaEntity> findById(Long id);
    long countByBookingId(Long bookingId);
    long countByBookingIdAndCheckedInTrue(Long bookingId);
}

