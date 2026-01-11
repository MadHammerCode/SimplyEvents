package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.repository.ActiveBookingRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.ActiveBookingMapper;
import at.fhv.simplyevents.persistence.model.ActiveBookingJpaEntity;
import at.fhv.simplyevents.persistence.springdata.ActiveBookingJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ActiveBookingRepositoryAdapter implements ActiveBookingRepositoryPort {

    private final ActiveBookingJpaRepository delegate;

    public ActiveBookingRepositoryAdapter(ActiveBookingJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public ActiveBooking save(ActiveBooking booking) {
        ActiveBookingJpaEntity saved = delegate.save(ActiveBookingMapper.toEntity(booking));
        return ActiveBookingMapper.toDomain(saved);
    }

    @Override
    public Optional<ActiveBooking> findById(Long id) {
        return delegate.findById(id).map(ActiveBookingMapper::toDomain);
    }

    @Override
    public Optional<ActiveBooking> findByBookingNumber(String bookingNumber) {
        return delegate.findByBookingNumber(bookingNumber).map(ActiveBookingMapper::toDomain);
    }

    @Override
    public int sumParticipantsByEventId(Long eventId) {
        return delegate.sumParticipantsByEventId(eventId);
    }

    @Override
    public int sumParticipantsByEventIdExcludingBooking(Long eventId, Long bookingId) {
        return delegate.sumParticipantsByEventIdExcludingBooking(eventId, bookingId);
    }

    @Override
    public void delete(ActiveBooking booking) {
        delegate.delete(ActiveBookingMapper.toEntity(booking));
    }

    @Override
    public List<ActiveBooking> findByEventId(Long eventId) {
        return delegate.findByEventId(eventId).stream().map(ActiveBookingMapper::toDomain).toList();
    }

    @Override
    public int sumParticipantsByEventIdAndDate(Long eventId, LocalDate date) {
        return delegate.sumParticipantsByEventIdAndDate(eventId, date);
    }
}
