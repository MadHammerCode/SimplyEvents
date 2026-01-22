package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.persistence.model.ActiveBookingJpaEntity;

public class ActiveBookingMapper {
    private ActiveBookingMapper() {}

    public static ActiveBooking toDomain(ActiveBookingJpaEntity entity) {
        if (entity == null) return null;
        ActiveBooking domain = new ActiveBooking();
        domain.setId(entity.getId());
        domain.setBookingNumber(entity.getBookingNumber());
        domain.setEventId(entity.getEventId());
        domain.setNumParticipants(entity.getNumParticipants());
        domain.setFirstName(entity.getFirstName());
        domain.setLastName(entity.getLastName());
        domain.setEmail(entity.getEmail());
        domain.setPhone(entity.getPhone());
        domain.setBookingType(entity.getBookingType());
        domain.setOptionDate(entity.getOptionDate());
        domain.setPaymentMethod(entity.getPaymentMethod());
        domain.setPriceTotal(entity.getPriceTotal());
        domain.setStatus(entity.getStatus());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    public static ActiveBookingJpaEntity toEntity(ActiveBooking domain) {
        if (domain == null) return null;
        ActiveBookingJpaEntity entity = new ActiveBookingJpaEntity();
        entity.setId(domain.getId());
        entity.setBookingNumber(domain.getBookingNumber());
        entity.setEventId(domain.getEventId());
        entity.setNumParticipants(domain.getNumParticipants());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setBookingType(domain.getBookingType());
        entity.setOptionDate(domain.getOptionDate());
        entity.setPaymentMethod(domain.getPaymentMethod());
        entity.setPriceTotal(domain.getPriceTotal());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
