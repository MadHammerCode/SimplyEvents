package at.fhv.simplyevents.rest.dto;

import java.math.BigDecimal;

public class BookingDtos {

    // Anfrage zum Buchen
    public record CreateBookingRequest(
            Long eventId,
            String firstName,
            String lastName,
            String email,
            String phone,
            Integer numParticipants,
            String paymentMethod
    ) {}

    // Antwort nach erfolgreicher Buchung
    public record BookingResponse(
            String bookingNumber,
            Long eventId,
            String eventTitle,
            String date,
            String time,
            String location,
            Integer numParticipants,
            BigDecimal priceTotal
    ) {}

    // Anfrage zum Canceln
    public record CancelBookingRequest(
            String bookingNumber,
            String cancelReason
    ) {}

    // Antwort nach erfolgreichem Canceln
    public record CancelledBookingResponse(
            String bookingNumber,
            String eventTitle,
            Integer numParticipants,
            String cancelReason
    ) {}
}
