package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEventEventId(Long eventId);
}

