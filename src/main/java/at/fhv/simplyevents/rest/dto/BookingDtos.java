package at.fhv.simplyevents.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;

public class BookingDtos {


    public record CreateBookingRequest(
            Long eventId,
            @NotBlank(message = "First name must not be blank")
            String firstName,
            @NotBlank(message = "Last name must not be blank")
            String lastName,
            @Email(message = "Email should be valid")
            @NotBlank(message = "Email must not be blank")
            String email,
            String phone,
            @Min(value = 1, message = "Number of participants must be at least 1")
            Integer numParticipants,
            String paymentMethod,
            LocalDate attendanceDate
    ) {}


    public record BookingResponse(
            Long bookingId,
            String bookingNumber,
            Long eventId,
            String eventTitle,
            String date,
            String time,
            String location,
            Integer numParticipants,
            BigDecimal priceTotal,
            LocalDate attendanceDate
    ) {}


    public record CancelBookingRequest(
            String bookingNumber,
            @NotBlank(message = "Cancel reason must not be blank")
            String cancelReason
    ) {}

    public record CancelBookingByIdRequest(
            @NotBlank(message = "Cancel reason must not be blank")
            String cancelReason
    ) {}

    public record CancelledBookingResponse(
            String bookingNumber,
            String eventTitle,
            Integer numParticipants,
            String cancelReason
    ) {}


    public record PendingBookingResponse(
            String pendingId,
            String bookingNumber,
            Long eventId,
            Integer numParticipants,
            BigDecimal priceTotal
    ) {}


    public record ConfirmBookingRequest(
            @NotBlank(message = "Pending ID must not be blank")
            String pendingId,
            @NotBlank(message = "Payment method must not be blank")
            String paymentMethod
    ) {}

    // DTO für die "My Bookings" Seite mit verschachteltem Event-Objekt
    public record MyBookingResponse(
            Long bookingId,
            String bookingNumber,
            Long eventId,
            EventInfo event,
            Integer numParticipants,
            BigDecimal priceTotal,
            String status,
            String bookingDate
    ) {}

    public record EventInfo(
            Long id,
            String title,
            String location,
            String date,
            String time
    ) {}
}
