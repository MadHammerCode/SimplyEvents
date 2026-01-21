package at.fhv.simplyevents.application.port.in;


import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.BookingDto;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.CreateParticipantCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingRequestCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingResponse;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.ParticipantDto;

import java.util.List;

public interface CheckInUseCase {
    List<ParticipantDto> findParticipantsForEvent(Long eventId);
    List<BookingDto> findBookingsForEvent(Long eventId);
    List<ParticipantDto> createParticipantsForBooking(Long bookingId, List<CreateParticipantCommand> dtos);
    ParticipantDto updateCheckedIn(Long participantId, boolean checkedIn);
    List<ParticipantDto> findParticipantsForBooking(Long bookingId);
    BookingDto updateBookingCapacity(Long bookingId, int requestedSeats);
    NewBookingResponse createBookingForEvent(Long eventId, NewBookingRequestCommand request);
    void deleteBooking(Long bookingId);
    void checkoutBooking(Long bookingId);

}
