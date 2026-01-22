package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.CancelledBooking;

import java.util.List;

public interface CancelledBookingRepositoryPort {
    CancelledBooking save(CancelledBooking cancelledBooking);
    List<CancelledBooking> findByEmail(String email);
}
