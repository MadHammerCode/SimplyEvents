package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Booking;
import at.fhv.simplyevents.domain.model.BookingType;
import at.fhv.simplyevents.domain.model.PaymentMethod;
import at.fhv.simplyevents.domain.model.Status;
import at.fhv.simplyevents.persistence.model.BookingJpaEntity;

public class BookingMapper {
    private BookingMapper() {}

    public static Booking toDomain(BookingJpaEntity e) {
        if (e == null) return null;
        return Booking.restore(
                e.getBookingId(),
                e.getBookingType() != null ? BookingType.valueOf(e.getBookingType()) : null,
                e.getStatus() != null ? Status.valueOf(e.getStatus()) : null,
                e.getNumParticipants(),
                e.getOptionDate(),
                e.getPaymentMethod() != null ? PaymentMethod.valueOf(e.getPaymentMethod()) : null,
                e.getPriceTotal(),
                e.getPaymentReference(),
                e.getEventId(),
                e.getUserId()
        );
    }

    public static BookingJpaEntity toEntity(Booking b) {
        if (b == null) return null;
        BookingJpaEntity e = new BookingJpaEntity();
        e.setBookingId(b.getBookingId());
        e.setBookingType(b.getBookingType() != null ? b.getBookingType().name() : null);
        e.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
        e.setNumParticipants(b.getNumParticipants());
        e.setOptionDate(b.getOptionDate());
        e.setPaymentMethod(b.getPaymentMethod() != null ? b.getPaymentMethod().name() : null);
        e.setPriceTotal(b.getPriceTotal());
        e.setPaymentReference(b.getPaymentReference());
        e.setEventId(b.getEventId());
        e.setUserId(b.getUserId());
        return e;
    }
}
