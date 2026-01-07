package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@Deprecated // Legacy repository removed from scan. Domain Event is not an @Entity. Use EventJpaRepository via adapter.
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIn(List<EventStatus> status);

    List<Event> findByStatus(EventStatus status);
}
