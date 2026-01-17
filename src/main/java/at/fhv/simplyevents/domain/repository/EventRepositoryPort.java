package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(Long id);
    boolean existsById(Long id);
    void deleteById(Long id);
    List<Event> findAll();
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusIn(List<EventStatus> status);
}

