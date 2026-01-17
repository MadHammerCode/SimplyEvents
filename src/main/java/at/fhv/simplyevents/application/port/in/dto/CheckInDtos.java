package at.fhv.simplyevents.application.port.in.dto;

import java.time.Instant;

public final class CheckInDtos {
    private CheckInDtos() {}

    public record ParticipantDto(
            Long id,
            String bookingNumber,
            String firstName,
            String lastName,
            String participantEmail,
            String bookerEmail,
            boolean checkedIn,
            Instant checkInTime
    ) {}

    public record BookingDto(
            Long bookingId,
            String bookingNumber,
            String bookerFirstName,
            String bookerLastName,
            String bookerEmail,
            int numParticipants,
            long participantsCreated,
            long participantsCheckedIn
    ) {}

    public record CreateParticipantCommand(String firstName, String lastName, String email) {}

    public record BookingCapacityCommand(int requestedSeats) {}

    public record NewBookingRequestCommand(String firstName, String lastName, String email, int seats) {}

    public record NewBookingResponse(BookingDto booking) {}
}

