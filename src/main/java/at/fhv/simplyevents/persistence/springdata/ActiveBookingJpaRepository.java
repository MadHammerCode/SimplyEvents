package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.ActiveBookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveBookingJpaRepository extends JpaRepository<ActiveBookingJpaEntity, Long> {
    Optional<ActiveBookingJpaEntity> findByBookingNumber(String bookingNumber);

    @Query("select coalesce(sum(a.numParticipants),0) from ActiveBookingJpaEntity a where a.eventId = :eventId")
    int sumParticipantsByEventId(@Param("eventId") Long eventId);

    @Query("select coalesce(sum(a.numParticipants),0) from ActiveBookingJpaEntity a where a.eventId = :eventId and a.id <> :bookingId")
    int sumParticipantsByEventIdExcludingBooking(@Param("eventId") Long eventId, @Param("bookingId") Long bookingId);

    List<ActiveBookingJpaEntity> findByEventId(Long eventId);

    @Query("SELECT COALESCE(SUM(b.numParticipants), 0) FROM ActiveBookingJpaEntity b " +
            "WHERE b.eventId = :eventId")
    int sumParticipantsByEventIdAndDate(@Param("eventId") Long eventId, @Param("date") LocalDate date);

    List<ActiveBookingJpaEntity> findByEmail(String email);

}
