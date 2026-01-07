package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.Booking;
import at.fhv.simplyevents.domain.repository.BookingRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.BookingMapper;
import at.fhv.simplyevents.persistence.springdata.BookingJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BookingRepositoryAdapter implements BookingRepositoryPort {

    private final BookingJpaRepository delegate;

    public BookingRepositoryAdapter(BookingJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Booking save(Booking booking) {
        var saved = delegate.save(BookingMapper.toEntity(booking));
        return BookingMapper.toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return delegate.findById(id).map(BookingMapper::toDomain);
    }
}

