package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Participant;
import at.fhv.simplyevents.persistence.model.ParticipantJpaEntity;

public class ParticipantMapper {
    private ParticipantMapper() {}

    public static Participant toDomain(ParticipantJpaEntity entity) {
        if (entity == null) return null;
        return Participant.restore(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.isCheckedIn(),
                entity.getCheckInTime(),
                entity.getEventId(),
                entity.getBookingId(),
                entity.getBookingNumber(),
                entity.getBookerEmail()
        );
    }

    public static ParticipantJpaEntity toEntity(Participant domain) {
        if (domain == null) return null;
        ParticipantJpaEntity e = new ParticipantJpaEntity();
        e.setId(domain.getId());
        e.setFirstName(domain.getFirstName());
        e.setLastName(domain.getLastName());
        e.setEmail(domain.getEmail());
        e.setCheckedIn(domain.isCheckedIn());
        e.setCheckInTime(domain.getCheckInTime());
        e.setEventId(domain.getEventId());
        e.setBookingId(domain.getBookingId());
        e.setBookingNumber(domain.getBookingNumber());
        e.setBookerEmail(domain.getBookerEmail());
        return e;
    }
}
