package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.BookingUseCase;
import at.fhv.simplyevents.application.port.in.EventUseCase;
import at.fhv.simplyevents.application.port.in.BookingUseCase.CreateBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.CancelBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.ConfirmBookingCommand;
import at.fhv.simplyevents.application.port.in.BookingUseCase.PendingBookingResponse;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.rest.dto.BookingDtos.BookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.MyBookingResponse;
import at.fhv.simplyevents.rest.dto.BookingDtos.EventInfo;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.CancelBookingByIdRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.ConfirmBookingRequest;
import at.fhv.simplyevents.rest.dto.BookingDtos.CreateBookingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private static final Logger logger = LoggerFactory.getLogger(BookingRestController.class);
    private final BookingUseCase bookingUseCase;
    private final EventUseCase eventUseCase;

    public BookingRestController(BookingUseCase bookingUseCase, EventUseCase eventUseCase) {
        this.bookingUseCase = bookingUseCase;
        this.eventUseCase = eventUseCase;
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
                        request.numParticipants(),
                        request.attendanceDate()
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
                    cancelled.getPriceTotal(),
                    null
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
                    booking.getPriceTotal(),
                    null
            ));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBookingById(@PathVariable Long bookingId,
                                               @Valid @RequestBody CancelBookingByIdRequest request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            ActiveBooking booking = bookingUseCase.getBooking(bookingId);
            CancelledBooking cancelled = bookingUseCase.cancelBooking(
                    new CancelBookingCommand(booking.getBookingNumber(), request.cancelReason())
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
                    cancelled.getPriceTotal(),
                    null
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
                    booking.getPriceTotal(),
                    null
            ));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my/bookings")
    public ResponseEntity<?> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Loading bookings for user: " + email);

        if (email == null || email.isEmpty()) {
            logger.warn("User not authenticated");
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            logger.info("Fetching bookings from database for email: " + email);
            List<?> bookings = bookingUseCase.getBookingsByEmail(email);
            logger.info("Found " + bookings.size() + " bookings");

            if (bookings.isEmpty()) {
                logger.info("No bookings found for user: " + email);
                return ResponseEntity.ok(List.of());
            }


            List<MyBookingResponse> bookingDtos = bookings.stream()
                    .map(this::convertToMyBookingResponse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Successfully converted " + bookingDtos.size() + " bookings to DTOs");
            return ResponseEntity.ok(bookingDtos);
        } catch (Exception e) {
            logger.error("Error loading bookings for email " + email + ": ", e);
            return ResponseEntity.status(500).body("Error loading bookings: " + e.getMessage());
        }
    }

    private MyBookingResponse convertToMyBookingResponse(Object booking) {
        if (booking instanceof ActiveBooking ab) {
            return convertActiveBookingToResponse(ab);
        } else if (booking instanceof CancelledBooking cb) {
            return convertCancelledBookingToResponse(cb);
        }
        return null;
    }

    private MyBookingResponse convertActiveBookingToResponse(ActiveBooking ab) {
        EventInfo eventInfo = loadEventInfo(ab.getEventId());
        return new MyBookingResponse(
                ab.getId(),
                ab.getBookingNumber(),
                ab.getEventId(),
                eventInfo,
                ab.getNumParticipants(),
                ab.getPriceTotal(),
                ab.getStatus(),
                null
        );
    }

    private MyBookingResponse convertCancelledBookingToResponse(CancelledBooking cb) {
        EventInfo eventInfo = loadEventInfo(cb.getEventId());
        return new MyBookingResponse(
                cb.getId(),
                cb.getBookingNumber(),
                cb.getEventId(),
                eventInfo,
                cb.getNumParticipants(),
                cb.getPriceTotal(),
                "CANCELLED",
                null
        );
    }

    private EventInfo loadEventInfo(Long eventId) {
        try {
            EventUseCase.EventResult event = eventUseCase.getEventById(eventId);
            return new EventInfo(
                    event.id(),
                    event.title(),
                    event.location(),
                    event.date(),
                    event.time()
            );
        } catch (Exception e) {
            logger.warn("Event mit ID " + eventId + " konnte nicht geladen werden: " + e.getMessage());
            return new EventInfo(
                    eventId,
                    "Event nicht mehr verfügbar",
                    "N/A",
                    "N/A",
                    "N/A"
            );
        }
    }
}
