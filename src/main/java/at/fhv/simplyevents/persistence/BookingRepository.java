package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COALESCE(SUM(b.numParticipants), 0) FROM BookingJpaEntity b " +
            "WHERE b.eventId = :eventId " +
            "AND b.attendanceDate = :date " +
            "AND b.status <> 'CANCELLED'")
    int countParticipantsForDate(@Param("eventId") Long eventId, @Param("date") LocalDate date);
}

