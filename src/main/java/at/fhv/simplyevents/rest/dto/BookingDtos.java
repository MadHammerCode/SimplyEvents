package at.fhv.simplyevents.rest.dto;

import java.math.BigDecimal;
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
            @NotBlank(message = "Payment method must not be blank")
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
            @NotBlank(message = "Cancel reason must not be blank")
            String cancelReason
    ) {}


    public record CancelledBookingResponse(
            String bookingNumber,
            String eventTitle,
            Integer numParticipants,
            String cancelReason
    ) {}
}
