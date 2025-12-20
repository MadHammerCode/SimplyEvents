package at.fhv.simplyevents.service;

import at.fhv.simplyevents.rest.dto.BookingDtos.*;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.Status;
import at.fhv.simplyevents.persistence.ActiveBookingRepository;
import at.fhv.simplyevents.persistence.CancelledBookingRepository;
import at.fhv.simplyevents.persistence.EventRepository;
import at.fhv.simplyevents.service.PendingBookingCache.PendingBooking;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final ActiveBookingRepository activeBookingRepository;
    private final CancelledBookingRepository cancelledBookingRepository;
    private final PendingBookingCache pendingBookingCache;

    public BookingService(EventRepository eventRepository,
                          ActiveBookingRepository activeBookingRepository,
                          CancelledBookingRepository cancelledBookingRepository,
                          PendingBookingCache pendingBookingCache) {
        this.eventRepository = eventRepository;
        this.activeBookingRepository = activeBookingRepository;
        this.cancelledBookingRepository = cancelledBookingRepository;
        this.pendingBookingCache = pendingBookingCache;
    }

    @Transactional
    public PendingBookingResponse createBooking(CreateBookingRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + request.eventId()));


        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int max = event.getMaxParticipants();
        int remaining = max - alreadyBooked;

        if (remaining <= 0) {
            throw new IllegalArgumentException("Event is fully booked.");
        }
        if (request.numParticipants() > remaining) {
            throw new IllegalArgumentException("Only " + remaining + " places left for this event.");
        }

        BigDecimal pricePerPerson = BigDecimal.valueOf(event.getPrice());
        BigDecimal total = pricePerPerson.multiply(BigDecimal.valueOf(request.numParticipants()));

        String bookingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        PendingBooking pending = pendingBookingCache.register(
                event.getEventId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.numParticipants(),
                total,
                bookingNumber
        );

        return new PendingBookingResponse(
                pending.id(),
                pending.bookingNumber(),
                event.getEventId(),
                request.numParticipants(),
                pending.priceTotal()
        );
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        ActiveBooking booking = activeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        Event event = booking.getEvent();
        return new BookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                event != null ? event.getEventId() : null,
                event != null ? event.getTitle() : null,
                event != null && event.getDate() != null ? event.getDate().toString() : null,
                "",
                event != null ? event.getLocation() : null,
                booking.getNumParticipants(),
                booking.getPriceTotal()
        );
    }

    @Transactional
    public CancelledBookingResponse cancelBooking(CancelBookingRequest request) {
        ActiveBooking booking = activeBookingRepository.findByBookingNumber(request.bookingNumber())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + request.bookingNumber()));

        Event event = booking.getEvent();


        CancelledBooking cancelled = new CancelledBooking();
        cancelled.setBookingNumber(booking.getBookingNumber());
        cancelled.setEvent(event);
        cancelled.setNumParticipants(booking.getNumParticipants());
        cancelled.setFirstName(booking.getFirstName());
        cancelled.setLastName(booking.getLastName());
        cancelled.setEmail(booking.getEmail());
        cancelled.setPhone(booking.getPhone());
        cancelled.setBookingType(booking.getBookingType());
        cancelled.setOptionDate(booking.getOptionDate());
        cancelled.setPaymentMethod(booking.getPaymentMethod());
        cancelled.setPriceTotal(booking.getPriceTotal());
        cancelled.setStatus("CANCELLED");
        cancelled.setCancelReason(request.cancelReason());
        cancelled.setCancelledAt(LocalDateTime.now());
        cancelled.setOriginalCreatedAt(booking.getCreatedAt());

        cancelledBookingRepository.save(cancelled);


        activeBookingRepository.delete(booking);


        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int remaining = event.getMaxParticipants() - alreadyBooked;


        event.setAvailableSlots(remaining);
        eventRepository.save(event);

        return new CancelledBookingResponse(
                cancelled.getBookingNumber(),
                event.getTitle(),
                cancelled.getNumParticipants(),
                cancelled.getCancelReason()
        );
    }

    @Transactional
    public BookingResponse confirmPendingBooking(ConfirmBookingRequest request) {
        PendingBooking pending = pendingBookingCache.get(request.pendingId())
                .orElseThrow(() -> new EntityNotFoundException("Pending booking not found: " + request.pendingId()));

        Event event = eventRepository.findById(pending.eventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found for pending booking: " + pending.eventId()));

        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int remaining = event.getMaxParticipants() - alreadyBooked;
        int requestedSeats = pending.numParticipants();

        if (remaining <= 0 || requestedSeats > remaining) {
            throw new IllegalArgumentException("Only " + Math.max(remaining, 0) + " places left for this event.");
        }

        ActiveBooking booking = new ActiveBooking();
        booking.setBookingNumber(pending.bookingNumber());
        booking.setEvent(event);
        booking.setNumParticipants(requestedSeats);
        booking.setFirstName(pending.firstName());
        booking.setLastName(pending.lastName());
        booking.setEmail(pending.email());
        booking.setPhone(pending.phone());
        booking.setPaymentMethod(request.paymentMethod());
        booking.setPriceTotal(pending.priceTotal());
        booking.setStatus(Status.PENDING_PAYMENT.name());
        booking.setCreatedAt(LocalDateTime.now());

        activeBookingRepository.save(booking);
        pendingBookingCache.remove(request.pendingId());

        int newRemaining = remaining - requestedSeats;
        event.setAvailableSlots(Math.max(newRemaining, 0));
        eventRepository.save(event);

        return new BookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                event.getEventId(),
                event.getTitle(),
                event.getDate() != null ? event.getDate().toString() : null,
                "",
                event.getLocation(),
                booking.getNumParticipants(),
                booking.getPriceTotal()
        );
    }
}

