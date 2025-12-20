package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<List<CheckInService.ParticipantDTO>> participantsForEvent(@PathVariable Long eventId) {
        List<CheckInService.ParticipantDTO> list = service.findParticipantsForEvent(eventId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/event/{eventId}/bookings")
    public ResponseEntity<List<CheckInService.BookingDTO>> bookingsForEvent(@PathVariable Long eventId) {
        List<CheckInService.BookingDTO> list = service.findBookingsForEvent(eventId);
        return ResponseEntity.ok(list);
    }

    public static record CheckInUpdateDTO(boolean checkedIn) {}

    @PostMapping("/bookings/{bookingId}/participants")
    public ResponseEntity<List<CheckInService.ParticipantDTO>> createParticipants(
            @PathVariable Long bookingId,
            @RequestBody List<CheckInService.CreateParticipantDTO> dtos
    ) {
        List<CheckInService.ParticipantDTO> created = service.createParticipantsForBooking(bookingId, dtos);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/participants/{id}")
    public ResponseEntity<?> updateParticipant(@PathVariable Long id, @RequestBody CheckInUpdateDTO dto) {
        var updated = service.updateCheckedIn(id, dto.checkedIn());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/bookings/{bookingId}/participants")
    public ResponseEntity<List<CheckInService.ParticipantDTO>> participantsForBooking(@PathVariable Long bookingId) {
        List<CheckInService.ParticipantDTO> list = service.findParticipantsForBooking(bookingId);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/bookings/{bookingId}/capacity")
    public ResponseEntity<CheckInService.BookingDTO> updateCapacity(
            @PathVariable Long bookingId,
            @RequestBody CheckInService.BookingCapacityDTO dto
    ) {
        var updated = service.updateBookingCapacity(bookingId, dto.requestedSeats());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/event/{eventId}/bookings")
    public ResponseEntity<CheckInService.NewBookingResponse> createBooking(
            @PathVariable Long eventId,
            @RequestBody CheckInService.NewBookingRequest request
    ) {
        var created = service.createBookingForEvent(eventId, request);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        service.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
