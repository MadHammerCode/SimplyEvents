package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.CheckedInParticipant;

public interface CheckedInParticipantRepositoryPort {
    CheckedInParticipant save(CheckedInParticipant participant);
    void deleteByBookingId(Long bookingId);
}

