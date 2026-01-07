package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.BookingUseCase;
import at.fhv.simplyevents.application.port.in.BookingUseCase.CreateBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.CancelBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.ConfirmBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.PendingBookingResponse;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.rest.dto.BookingDtos.BookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.ConfirmBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.CreateBookingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingUseCase bookingUseCase;

    public BookingRestController(BookingUseCase bookingUseCase) {
        this.bookingUseCase = bookingUseCase;
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

        PendingBookingResponse response = bookingUseCase.createBooking(
                new CreateBookingCommand(
                        request.eventId(),
                        request.firstName(),
                        request.lastName(),
                        request.email(),
                        request.phone(),
                        request.numParticipants()
                )
        );
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

        try {
            CancelledBooking cancelled = bookingUseCase.cancelBooking(
                    new CancelBookingCommand(request.bookingNumber(), request.cancelReason())
            );
            return ResponseEntity.ok(new BookingResponse(
                    null,
                    cancelled.getBookingNumber(),
                    cancelled.getEventId(),
                    null,
                    null,
                    "",
                    null,
                    cancelled.getNumParticipants(),
                    cancelled.getPriceTotal()
            ));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPendingBooking(@Valid @RequestBody ConfirmBookingRequest request,
                                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            ActiveBooking booking = bookingUseCase.confirmPendingBooking(
                    new ConfirmBookingCommand(request.pendingId(), request.paymentMethod())
            );
            return ResponseEntity.ok(new BookingResponse(
                    booking.getId(),
                    booking.getBookingNumber(),
                    booking.getEventId(),
                    null,
                    null,
                    "",
                    null,
                    booking.getNumParticipants(),
                    booking.getPriceTotal()
            ));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {
        try {
            ActiveBooking booking = bookingUseCase.getBooking(bookingId);
            return ResponseEntity.ok(new BookingResponse(
                    booking.getId(),
                    booking.getBookingNumber(),
                    booking.getEventId(),
                    null,
                    null,
                    "",
                    null,
                    booking.getNumParticipants(),
                    booking.getPriceTotal()
            ));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
