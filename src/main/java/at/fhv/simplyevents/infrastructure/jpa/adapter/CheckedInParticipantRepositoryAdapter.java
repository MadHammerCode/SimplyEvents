package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.CheckedInParticipant;
import at.fhv.simplyevents.domain.repository.CheckedInParticipantRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.CheckedInParticipantMapper;
import at.fhv.simplyevents.persistence.springdata.CheckedInParticipantJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class CheckedInParticipantRepositoryAdapter implements CheckedInParticipantRepositoryPort {

    private final CheckedInParticipantJpaRepository delegate;

    public CheckedInParticipantRepositoryAdapter(CheckedInParticipantJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CheckedInParticipant save(CheckedInParticipant participant) {
        var saved = delegate.save(CheckedInParticipantMapper.toEntity(participant));
        return CheckedInParticipantMapper.toDomain(saved);
    }

    @Override
    public void deleteByBookingId(Long bookingId) {
        delegate.deleteByBookingId(bookingId);
    }
}

