package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Participant;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CheckedInParticipant;
import at.fhv.simplyevents.persistence.ParticipantRepository;
import at.fhv.simplyevents.persistence.EventRepository;
import at.fhv.simplyevents.persistence.ActiveBookingRepository;
import at.fhv.simplyevents.persistence.CheckedInParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final ParticipantRepository participants;
    private final EventRepository events;
    private final ActiveBookingRepository activeBookings;
    private final CheckedInParticipantRepository checkedInParticipants;

    public CheckInService(ParticipantRepository participants, EventRepository events, ActiveBookingRepository activeBookings,
                          CheckedInParticipantRepository checkedInParticipants) {
        this.participants = participants;
        this.events = events;
        this.activeBookings = activeBookings;
        this.checkedInParticipants = checkedInParticipants;
    }

    public static record ParticipantDTO(
            Long id,
            String bookingNumber,
            String firstName,
            String lastName,
            String participantEmail,
            String bookerEmail,
            boolean checkedIn,
            Instant checkInTime
    ) {}

    public List<ParticipantDTO> findParticipantsForEvent(Long eventId) {
        // ensure event exists
        events.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        return participants.findByEventEventId(eventId).stream()
                .map(p -> new ParticipantDTO(
                        p.getId(),
                        p.getBooking() != null ? p.getBooking().getBookingNumber() : null,
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getBooking() != null ? p.getBooking().getEmail() : null,
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    public ParticipantDTO updateCheckedIn(Long participantId, boolean checkedIn) {
        Participant p = participants.findById(participantId).orElseThrow(() -> new NoSuchElementException("Participant not found"));
        p.setCheckedIn(checkedIn);
        p.setCheckInTime(checkedIn ? Instant.now() : null);
        participants.save(p);
        return new ParticipantDTO(
                p.getId(),
                p.getBooking() != null ? p.getBooking().getBookingNumber() : null,
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getBooking() != null ? p.getBooking().getEmail() : null,
                p.isCheckedIn(),
                p.getCheckInTime()
        );
    }

    public List<BookingDTO> findBookingsForEvent(Long eventId) {
        // ensure event exists
        events.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));

        return activeBookings.findByEventEventId(eventId).stream()
                .map(b -> {
                    long created = participants.countByBookingId(b.getId());
                    long checked = participants.countByBookingIdAndCheckedInTrue(b.getId());
                    int seats = b.getNumParticipants() != null ? b.getNumParticipants() : 0;
                    return new BookingDTO(
                            b.getId(),
                            b.getBookingNumber(),
                            b.getFirstName(),
                            b.getLastName(),
                            b.getEmail(),
                            seats,
                            created,
                            checked
                    );
                })
                .collect(Collectors.toList());
    }

    public List<ParticipantDTO> createParticipantsForBooking(Long bookingId, List<CreateParticipantDTO> createDtos) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        int max = booking.getNumParticipants() != null ? booking.getNumParticipants() : 0;

        if (createDtos == null || createDtos.isEmpty()) {
            throw new IllegalArgumentException("No participants provided");
        }

        if (createDtos.size() > max) {
            throw new IllegalArgumentException("Too many participants. Capacity: " + max);
        }

        participants.deleteByBookingId(bookingId);
        checkedInParticipants.deleteByBookingId(bookingId);

        Instant now = Instant.now();
        for (CreateParticipantDTO dto : createDtos) {
            // Participant email is optional; fall back to booker email if not provided
            String participantEmail = dto.email();
            if (participantEmail != null) {
                participantEmail = participantEmail.trim();
            }
            if (participantEmail == null || participantEmail.isBlank()) {
                participantEmail = booking.getEmail() != null ? booking.getEmail().trim() : null;
            }

            Participant p = new Participant();
            p.setEvent(booking.getEvent());
            p.setBooking(booking);
            p.setFirstName(dto.firstName());
            p.setLastName(dto.lastName());
            p.setEmail(participantEmail);
            p.setCheckedIn(true);
            p.setCheckInTime(now);
            participants.save(p);

            CheckedInParticipant cip = new CheckedInParticipant();
            cip.setEvent(booking.getEvent());
            cip.setBooking(booking);
            cip.setFirstName(dto.firstName());
            cip.setLastName(dto.lastName());
            cip.setEmail(participantEmail);
            cip.setCheckedIn(true);
            cip.setCheckInTime(now);
            checkedInParticipants.save(cip);
        }

        return participants.findByBookingId(bookingId).stream()
                .map(p -> new ParticipantDTO(
                        p.getId(),
                        p.getBooking() != null ? p.getBooking().getBookingNumber() : null,
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getBooking() != null ? p.getBooking().getEmail() : null,
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    public List<ParticipantDTO> findParticipantsForBooking(Long bookingId) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        return participants.findByBookingId(bookingId).stream()
                .map(p -> new ParticipantDTO(
                        p.getId(),
                        booking.getBookingNumber(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        booking.getEmail(),
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    public static record BookingDTO(
            Long bookingId,
            String bookingNumber,
            String bookerFirstName,
            String bookerLastName,
            String bookerEmail,
            int numParticipants,
            long participantsCreated,
            long participantsCheckedIn
    ) {}

    public static record CreateParticipantDTO(String firstName, String lastName, String email) {}

    public static record BookingCapacityDTO(int requestedSeats) {}

    public BookingDTO updateBookingCapacity(Long bookingId, int requestedSeats) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        if (requestedSeats <= 0) {
            throw new IllegalArgumentException("Seat count must be greater than zero");
        }

        Event event = booking.getEvent();
        int alreadyBookedOther = activeBookings.sumParticipantsByEventIdExcludingBooking(event.getEventId(), bookingId);
        int max = event.getMaxParticipants();
        int possible = max - alreadyBookedOther;
        if (requestedSeats > possible) {
            throw new IllegalArgumentException("Only " + possible + " seats available for this event.");
        }

        booking.setNumParticipants(requestedSeats);
        activeBookings.save(booking);

        long checked = participants.countByBookingIdAndCheckedInTrue(bookingId);
        return new BookingDTO(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getFirstName(),
                booking.getLastName(),
                booking.getEmail(),
                requestedSeats,
                participants.countByBookingId(bookingId),
                checked
        );
    }

    public static record NewBookingRequest(String firstName, String lastName, String email, int seats) {}
    public static record NewBookingResponse(BookingDTO booking) {}

    public NewBookingResponse createBookingForEvent(Long eventId, NewBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Missing booking data");
        }
        if (request.seats() <= 0) {
            throw new IllegalArgumentException("Seat count must be greater than zero");
        }
        Event event = events.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        int alreadyBooked = activeBookings.sumParticipantsByEventId(eventId);
        int remaining = event.getMaxParticipants() - alreadyBooked;
        if (remaining <= 0) {
            throw new IllegalArgumentException("Event is fully booked.");
        }
        if (request.seats() > remaining) {
            throw new IllegalArgumentException("Only " + remaining + " seats available.");
        }

        ActiveBooking booking = new ActiveBooking();
        booking.setBookingNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        booking.setEvent(event);
        booking.setNumParticipants(request.seats());
        booking.setFirstName(request.firstName());
        booking.setLastName(request.lastName());
        booking.setEmail(request.email());
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());
        activeBookings.save(booking);

        event.setAvailableSlots(remaining - request.seats());
        events.save(event);

        BookingDTO dto = new BookingDTO(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getFirstName(),
                booking.getLastName(),
                booking.getEmail(),
                booking.getNumParticipants(),
                participants.countByBookingId(booking.getId()),
                participants.countByBookingIdAndCheckedInTrue(booking.getId())
        );
        return new NewBookingResponse(dto);
    }
}
