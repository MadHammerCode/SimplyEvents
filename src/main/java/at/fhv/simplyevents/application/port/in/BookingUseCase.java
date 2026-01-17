package at.fhv.simplyevents.application.port.in;

import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BookingUseCase {
    PendingBookingResponse createBooking(CreateBookingCommand command);
    CancelledBooking cancelBooking(CancelBookingCommand command);
    ActiveBooking confirmPendingBooking(ConfirmBookingCommand command);
    ActiveBooking getBooking(Long bookingId);

    record CreateBookingCommand(Long eventId, String firstName, String lastName, String email, String phone, int numParticipants, LocalDate attendanceDate) {}
    record PendingBookingResponse(String pendingId, String bookingNumber, Long eventId, int numParticipants, BigDecimal priceTotal) {}
    record CancelBookingCommand(String bookingNumber, String cancelReason) {}
    record ConfirmBookingCommand(String pendingId, String paymentMethod) {}
}
