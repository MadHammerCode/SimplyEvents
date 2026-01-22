package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.domain.repository.CancelledBookingRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.CancelledBookingMapper;
import at.fhv.simplyevents.persistence.springdata.CancelledBookingJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CancelledBookingRepositoryAdapter implements CancelledBookingRepositoryPort {

    private final CancelledBookingJpaRepository delegate;

    public CancelledBookingRepositoryAdapter(CancelledBookingJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CancelledBooking save(CancelledBooking cancelledBooking) {
        var saved = delegate.save(CancelledBookingMapper.toEntity(cancelledBooking));
        return CancelledBookingMapper.toDomain(saved);
    }

    @Override
    public List<CancelledBooking> findByEmail(String email) {
        return delegate.findByEmail(email).stream().map(CancelledBookingMapper::toDomain).toList();
    }
}

