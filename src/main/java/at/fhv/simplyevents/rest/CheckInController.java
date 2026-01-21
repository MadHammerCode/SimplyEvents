package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.port.in.CheckInUseCase;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.BookingCapacityCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.BookingDto;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.CreateParticipantCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingRequestCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingResponse;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.ParticipantDto;
import at.fhv.simplyevents.rest.dto.CheckInRestDtos.CheckInUpdateDto;
import at.fhv.simplyevents.rest.dto.CheckInRestDtos.NewBookingRequestDto;
import at.fhv.simplyevents.rest.dto.CheckInRestDtos.ParticipantCreateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInUseCase service;

    public CheckInController(CheckInUseCase service) {
        this.service = service;
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<List<ParticipantDto>> participantsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.findParticipantsForEvent(eventId));
    }

    @GetMapping("/event/{eventId}/bookings")
    public ResponseEntity<List<BookingDto>> bookingsForEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(service.findBookingsForEvent(eventId));
    }

    @PostMapping("/bookings/{bookingId}/participants")
    public ResponseEntity<List<ParticipantDto>> createParticipants(
            @PathVariable Long bookingId,
            @RequestBody List<ParticipantCreateDto> dtos
    ) {
        List<CreateParticipantCommand> commands = dtos.stream()
                .map(dto -> new CreateParticipantCommand(dto.firstName(), dto.lastName(), dto.email()))
                .toList();
        return ResponseEntity.ok(service.createParticipantsForBooking(bookingId, commands));
    }

    @PutMapping("/participants/{id}")
    public ResponseEntity<ParticipantDto> updateParticipant(@PathVariable Long id, @RequestBody CheckInUpdateDto dto) {
        var updated = service.updateCheckedIn(id, dto.checkedIn());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/bookings/{bookingId}/participants")
    public ResponseEntity<List<ParticipantDto>> participantsForBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(service.findParticipantsForBooking(bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/capacity")
    public ResponseEntity<BookingDto> updateCapacity(
            @PathVariable Long bookingId,
            @RequestBody BookingCapacityCommand dto
    ) {
        var updated = service.updateBookingCapacity(bookingId, dto.requestedSeats());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/event/{eventId}/bookings")
    public ResponseEntity<NewBookingResponse> createBooking(
            @PathVariable Long eventId,
            @RequestBody NewBookingRequestDto request
    ) {
        NewBookingRequestCommand cmd = new NewBookingRequestCommand(request.firstName(), request.lastName(), request.email(), request.seats());
        var created = service.createBookingForEvent(eventId, cmd);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        service.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bookings/{bookingId}/checkout")
    public ResponseEntity<Void> checkoutBooking(@PathVariable Long bookingId) {
        service.checkoutBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}