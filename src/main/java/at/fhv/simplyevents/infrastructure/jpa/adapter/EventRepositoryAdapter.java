package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.domain.repository.EventRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.EventMapper;
import at.fhv.simplyevents.persistence.model.EventJpaEntity;
import at.fhv.simplyevents.persistence.springdata.EventJpaRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final EventJpaRepository delegate;

    public EventRepositoryAdapter(EventJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Event save(Event event) {
        EventJpaEntity saved = delegate.save(EventMapper.toEntity(event));
        return EventMapper.toDomain(saved);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return delegate.findById(id).map(EventMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return delegate.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        try {
            delegate.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw NotFoundException.forEntity("Event", id);
        }
    }

    @Override
    public List<Event> findAll() {
        return delegate.findAll().stream().map(EventMapper::toDomain).toList();
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return delegate.findByStatus(status).stream().map(EventMapper::toDomain).toList();
    }

    @Override
    public List<Event> findByStatusIn(List<EventStatus> status) {
        return delegate.findByStatusIn(status).stream().map(EventMapper::toDomain).toList();
    }
}
