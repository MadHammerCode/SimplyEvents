package at.fhv.simplyevents.rest.dto;

import java.math.BigDecimal;

public class BookingDtos {


    public record CreateBookingRequest(
            Long eventId,
            String firstName,
            String lastName,
            String email,
            String phone,
            Integer numParticipants,
            String paymentMethod
    ) {}


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


    public record CancelBookingRequest(
            String bookingNumber,
            String cancelReason
    ) {}


    public record CancelledBookingResponse(
            String bookingNumber,
            String eventTitle,
            Integer numParticipants,
            String cancelReason
    ) {}
}
