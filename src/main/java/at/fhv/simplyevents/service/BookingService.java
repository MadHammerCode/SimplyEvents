package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.BookingUseCase;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CancelledBooking;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.Status;
import at.fhv.simplyevents.domain.repository.ActiveBookingRepositoryPort;
import at.fhv.simplyevents.domain.repository.CancelledBookingRepositoryPort;
import at.fhv.simplyevents.domain.repository.EventRepositoryPort;
import at.fhv.simplyevents.service.PendingBookingCache.PendingBooking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService implements BookingUseCase {

    private final EventRepositoryPort eventRepository;
    private final ActiveBookingRepositoryPort activeBookingRepository;
    private final CancelledBookingRepositoryPort cancelledBookingRepository;
    private final PendingBookingCache pendingBookingCache;

    public BookingService(EventRepositoryPort eventRepository,
                          ActiveBookingRepositoryPort activeBookingRepository,
                          CancelledBookingRepositoryPort cancelledBookingRepository,
                          PendingBookingCache pendingBookingCache) {
        this.eventRepository = eventRepository;
        this.activeBookingRepository = activeBookingRepository;
        this.cancelledBookingRepository = cancelledBookingRepository;
        this.pendingBookingCache = pendingBookingCache;
    }

    @Transactional
    @Override
    public PendingBookingResponse createBooking(CreateBookingCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> NotFoundException.forEntity("Event", command.eventId()));

        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int max = event.getMaxParticipants();
        int remaining = max - alreadyBooked;

        if (remaining <= 0) {
            throw new IllegalArgumentException("Event is fully booked.");
        }
        if (command.numParticipants() > remaining) {
            throw new IllegalArgumentException("Only " + remaining + " places left for this event.");
        }

        BigDecimal pricePerPerson = BigDecimal.valueOf(event.getPrice());
        BigDecimal total = pricePerPerson.multiply(BigDecimal.valueOf(command.numParticipants()));

        String bookingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        PendingBooking pending = pendingBookingCache.register(
                event.getEventId(),
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.numParticipants(),
                total,
                bookingNumber
        );

        return new PendingBookingResponse(
                pending.id(),
                pending.bookingNumber(),
                event.getEventId(),
                command.numParticipants(),
                pending.priceTotal()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ActiveBooking getBooking(Long bookingId) {
        ActiveBooking booking = activeBookingRepository.findById(bookingId)
                .orElseThrow(() -> NotFoundException.forEntity("Booking", bookingId));
        Event event = eventRepository.findById(booking.getEventId())
                .orElse(null);
        // enrich booking with event data if needed elsewhere
        return booking;
    }

    @Transactional
    @Override
    public CancelledBooking cancelBooking(CancelBookingCommand command) {
        ActiveBooking booking = activeBookingRepository.findByBookingNumber(command.bookingNumber())
                .orElseThrow(() -> NotFoundException.forEntityAndField("Booking", "bookingNumber", command.bookingNumber()));

        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() -> NotFoundException.forEntity("Event", booking.getEventId()));

        CancelledBooking cancelled = new CancelledBooking();
        cancelled.setBookingNumber(booking.getBookingNumber());
        cancelled.setEventId(event.getEventId());
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
        cancelled.setCancelReason(command.cancelReason());
        cancelled.setCancelledAt(LocalDateTime.now());
        cancelled.setOriginalCreatedAt(booking.getCreatedAt());

        cancelledBookingRepository.save(cancelled);
        activeBookingRepository.delete(booking);

        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int remaining = event.getMaxParticipants() - alreadyBooked;
        event.setAvailableSlots(remaining);
        eventRepository.save(event);

        return cancelled;
    }

    @Transactional
    @Override
    public ActiveBooking confirmPendingBooking(ConfirmBookingCommand command) {
        PendingBooking pending = pendingBookingCache.get(command.pendingId())
                .orElseThrow(() -> NotFoundException.forEntity("PendingBooking", command.pendingId()));

        Event event = eventRepository.findById(pending.eventId())
                .orElseThrow(() -> NotFoundException.forEntity("Event", pending.eventId()));

        int alreadyBooked = activeBookingRepository.sumParticipantsByEventId(event.getEventId());
        int remaining = event.getMaxParticipants() - alreadyBooked;
        int requestedSeats = pending.numParticipants();

        if (remaining <= 0 || requestedSeats > remaining) {
            throw new IllegalArgumentException("Only " + Math.max(remaining, 0) + " places left for this event.");
        }

        ActiveBooking booking = new ActiveBooking();
        booking.setBookingNumber(pending.bookingNumber());
        booking.setEventId(event.getEventId());
        booking.setNumParticipants(requestedSeats);
        booking.setFirstName(pending.firstName());
        booking.setLastName(pending.lastName());
        booking.setEmail(pending.email());
        booking.setPhone(pending.phone());
        booking.setPaymentMethod(command.paymentMethod());
        booking.setPriceTotal(pending.priceTotal());
        booking.setStatus(Status.PENDING_PAYMENT.name());
        booking.setCreatedAt(LocalDateTime.now());

        ActiveBooking savedBooking = activeBookingRepository.save(booking);
        pendingBookingCache.remove(command.pendingId());

        int newRemaining = remaining - requestedSeats;
        event.setAvailableSlots(Math.max(newRemaining, 0));
        eventRepository.save(event);

        return savedBooking;
    }
}
