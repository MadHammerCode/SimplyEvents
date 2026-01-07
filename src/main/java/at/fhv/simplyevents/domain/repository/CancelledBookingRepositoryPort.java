package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.CancelledBooking;

public interface CancelledBookingRepositoryPort {
    CancelledBooking save(CancelledBooking cancelledBooking);
}
