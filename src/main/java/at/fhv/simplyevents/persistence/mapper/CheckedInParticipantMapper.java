package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.CheckedInParticipant;
import at.fhv.simplyevents.persistence.model.CheckedInParticipantJpaEntity;

public class CheckedInParticipantMapper {
    private CheckedInParticipantMapper() {}

    public static CheckedInParticipant toDomain(CheckedInParticipantJpaEntity entity) {
        if (entity == null) return null;
        CheckedInParticipant c = new CheckedInParticipant();
        c.setId(entity.getId());
        c.setFirstName(entity.getFirstName());
        c.setLastName(entity.getLastName());
        c.setEmail(entity.getEmail());
        c.setCheckedIn(entity.isCheckedIn());
        c.setCheckInTime(entity.getCheckInTime());
        c.setEventId(entity.getEventId());
        c.setBookingId(entity.getBookingId());
        c.setBookingNumber(entity.getBookingNumber());
        return c;
    }

    public static CheckedInParticipantJpaEntity toEntity(CheckedInParticipant domain) {
        if (domain == null) return null;
        CheckedInParticipantJpaEntity e = new CheckedInParticipantJpaEntity();
        e.setId(domain.getId());
        e.setFirstName(domain.getFirstName());
        e.setLastName(domain.getLastName());
        e.setEmail(domain.getEmail());
        e.setCheckedIn(domain.isCheckedIn());
        e.setCheckInTime(domain.getCheckInTime());
        e.setEventId(domain.getEventId());
        e.setBookingId(domain.getBookingId());
        e.setBookingNumber(domain.getBookingNumber());
        return e;
    }
}

