package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.Participant;
import at.fhv.simplyevents.domain.repository.ParticipantRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.ParticipantMapper;
import at.fhv.simplyevents.persistence.springdata.ParticipantJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ParticipantRepositoryAdapter implements ParticipantRepositoryPort {

    private final ParticipantJpaRepository delegate;

    public ParticipantRepositoryAdapter(ParticipantJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Participant save(Participant participant) {
        var saved = delegate.save(ParticipantMapper.toEntity(participant));
        return ParticipantMapper.toDomain(saved);
    }

    @Override
    public void deleteByBookingId(Long bookingId) {
        delegate.deleteByBookingId(bookingId);
    }

    @Override
    public List<Participant> findByEventId(Long eventId) {
        return delegate.findByEventId(eventId).stream().map(ParticipantMapper::toDomain).toList();
    }

    @Override
    public List<Participant> findByBookingId(Long bookingId) {
        return delegate.findByBookingId(bookingId).stream().map(ParticipantMapper::toDomain).toList();
    }

    @Override
    public Optional<Participant> findById(Long id) {
        return delegate.findById(id).map(ParticipantMapper::toDomain);
    }

    @Override
    public long countByBookingId(Long bookingId) {
        return delegate.countByBookingId(bookingId);
    }

    @Override
    public long countByBookingIdAndCheckedInTrue(Long bookingId) {
        return delegate.countByBookingIdAndCheckedInTrue(bookingId);
    }
}

