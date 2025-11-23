package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.BookingDtos.BookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelledBookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.CreateBookingRequest;
import at.fhv.simplyevents.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {

        if (request.eventId() == null ||
                request.firstName() == null || request.firstName().isBlank() ||
                request.lastName() == null || request.lastName().isBlank() ||
                request.email() == null || request.email().isBlank() ||
                request.numParticipants() == null || request.numParticipants() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancelledBookingResponse> cancelBooking(@RequestBody CancelBookingRequest request) {
        if (request.bookingNumber() == null || request.bookingNumber().isBlank() ||
                request.cancelReason() == null || request.cancelReason().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        CancelledBookingResponse response = bookingService.cancelBooking(request);
        return ResponseEntity.ok(response);
    }
}
