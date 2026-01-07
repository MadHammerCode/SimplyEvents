package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.CheckedInParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckedInParticipantJpaRepository extends JpaRepository<CheckedInParticipantJpaEntity, Long> {
    void deleteByBookingId(Long bookingId);
}

