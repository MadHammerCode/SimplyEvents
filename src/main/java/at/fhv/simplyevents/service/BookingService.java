package at.fhv.simplyevents.service;

import at.fhv.simplyevents.rest.dto.BookingDtos.*;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.persistence.BookRepository;
import at.fhv.simplyevents.persistence.CancelledBookingRepository;
import at.fhv.simplyevents.persistence.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private final EventRepository eventRepository;
    private final BookRepository activeBookingRepository;
    private final CancelledBookingRepository cancelledBookingRepository;

    public BookingService(EventRepository eventRepository,
                          BookRepository activeBookingRepository,
                          CancelledBookingRepository cancelledBookingRepository) {
        this.eventRepository = eventRepository;
        this.activeBookingRepository = activeBookingRepository;
        this.cancelledBookingRepository = cancelledBookingRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + request.eventId()));


        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int max = event.getMaxParticipants();
        int remaining = max - alreadyBooked;

        event.setAvailableSlots(Math.max(remaining, 0));
        eventRepository.save(event);

        if (remaining <= 0) {
            throw new IllegalArgumentException("Event is fully booked.");
        }
        if (request.numParticipants() > remaining) {
            throw new IllegalArgumentException("Only " + remaining + " places left for this event.");
        }


        BigDecimal pricePerPerson = BigDecimal.valueOf(event.getPrice());
        BigDecimal total = pricePerPerson.multiply(BigDecimal.valueOf(request.numParticipants()));


        String bookingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        ActiveBooking booking = new ActiveBooking();
        booking.setBookingNumber(bookingNumber);
        booking.setEvent(event);
        booking.setNumParticipants(request.numParticipants());
        booking.setFirstName(request.firstName());
        booking.setLastName(request.lastName());
        booking.setEmail(request.email());
        booking.setPhone(request.phone());
        booking.setPaymentMethod(request.paymentMethod());
        booking.setPriceTotal(total);
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());

        activeBookingRepository.save(booking);


        event.setAvailableSlots(remaining - request.numParticipants());
        eventRepository.save(event);

        return new BookingResponse(
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
}