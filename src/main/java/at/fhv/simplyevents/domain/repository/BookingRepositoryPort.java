package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Booking;

import java.util.Optional;

public interface BookingRepositoryPort {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
}

