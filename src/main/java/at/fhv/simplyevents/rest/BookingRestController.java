package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.BookingDtos.BookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelledBookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.CreateBookingRequest;
import at.fhv.simplyevents.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking(@Valid @RequestBody CancelBookingRequest request,
                                           BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        CancelledBookingResponse response = bookingService.cancelBooking(request);
        return ResponseEntity.ok(response);
    }
}
