package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.persistence.model.CancelledBookingJpaEntity;

public class CancelledBookingMapper {
    private CancelledBookingMapper() {}

    public static CancelledBooking toDomain(CancelledBookingJpaEntity e) {
        if (e == null) return null;
        CancelledBooking c = new CancelledBooking();
        c.setId(e.getId());
        c.setBookingNumber(e.getBookingNumber());
        c.setEventId(e.getEventId());
        c.setNumParticipants(e.getNumParticipants());
        c.setFirstName(e.getFirstName());
        c.setLastName(e.getLastName());
        c.setEmail(e.getEmail());
        c.setPhone(e.getPhone());
        c.setBookingType(e.getBookingType());
        c.setOptionDate(e.getOptionDate());
        c.setPaymentMethod(e.getPaymentMethod());
        c.setPriceTotal(e.getPriceTotal());
        c.setStatus(e.getStatus());
        c.setCancelReason(e.getCancelReason());
        c.setCancelledAt(e.getCancelledAt());
        c.setOriginalCreatedAt(e.getOriginalCreatedAt());
        return c;
    }

    public static CancelledBookingJpaEntity toEntity(CancelledBooking c) {
        if (c == null) return null;
        CancelledBookingJpaEntity e = new CancelledBookingJpaEntity();
        e.setId(c.getId());
        e.setBookingNumber(c.getBookingNumber());
        e.setEventId(c.getEventId());
        e.setNumParticipants(c.getNumParticipants());
        e.setFirstName(c.getFirstName());
        e.setLastName(c.getLastName());
        e.setEmail(c.getEmail());
        e.setPhone(c.getPhone());
        e.setBookingType(c.getBookingType());
        e.setOptionDate(c.getOptionDate());
        e.setPaymentMethod(c.getPaymentMethod());
        e.setPriceTotal(c.getPriceTotal());
        e.setStatus(c.getStatus());
        e.setCancelReason(c.getCancelReason());
        e.setCancelledAt(c.getCancelledAt());
        e.setOriginalCreatedAt(c.getOriginalCreatedAt());
        return e;
    }
}

