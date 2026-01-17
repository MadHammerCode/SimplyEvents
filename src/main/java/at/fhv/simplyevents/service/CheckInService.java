package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.CheckInUseCase;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.BookingDto;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.CreateParticipantCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingRequestCommand;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.NewBookingResponse;
import at.fhv.simplyevents.application.port.in.dto.CheckInDtos.ParticipantDto;
import at.fhv.simplyevents.domain.model.Participant;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.ActiveBooking;
import at.fhv.simplyevents.domain.model.CheckedInParticipant;
import at.fhv.simplyevents.domain.repository.ActiveBookingRepositoryPort;
import at.fhv.simplyevents.domain.repository.CheckedInParticipantRepositoryPort;
import at.fhv.simplyevents.domain.repository.EventRepositoryPort;
import at.fhv.simplyevents.domain.repository.ParticipantRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckInService implements CheckInUseCase {

    private final ParticipantRepositoryPort participants;
    private final EventRepositoryPort events;
    private final ActiveBookingRepositoryPort activeBookings;
    private final CheckedInParticipantRepositoryPort checkedInParticipants;

    public CheckInService(ParticipantRepositoryPort participants, EventRepositoryPort events, ActiveBookingRepositoryPort activeBookings,
                          CheckedInParticipantRepositoryPort checkedInParticipants) {
        this.participants = participants;
        this.events = events;
        this.activeBookings = activeBookings;
        this.checkedInParticipants = checkedInParticipants;
    }

    @Override
    public List<ParticipantDto> findParticipantsForEvent(Long eventId) {
        // ensure event exists
        events.findById(eventId).orElseThrow(() -> NotFoundException.forEntity("Event", eventId));
        return participants.findByEventId(eventId).stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getBookingNumber(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getBookerEmail(),
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ParticipantDto updateCheckedIn(Long participantId, boolean checkedIn) {
        Participant p = participants.findById(participantId).orElseThrow(() -> NotFoundException.forEntity("Participant", participantId));
        if (checkedIn) {
            p.markCheckedIn(Instant.now());
        } else {
            p.markCheckedOut();
        }
        participants.save(p);
        return new ParticipantDto(
                p.getId(),
                p.getBookingNumber(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getBookerEmail(),
                p.isCheckedIn(),
                p.getCheckInTime()
        );
    }

    @Override
    public List<BookingDto> findBookingsForEvent(Long eventId) {
        // ensure event exists
        events.findById(eventId).orElseThrow(() -> NotFoundException.forEntity("Event", eventId));

        return activeBookings.findByEventId(eventId).stream()
                .map(b -> {
                    long created = participants.countByBookingId(b.getId());
                    long checked = participants.countByBookingIdAndCheckedInTrue(b.getId());
                    int seats = b.getNumParticipants() != null ? b.getNumParticipants() : 0;
                    return new BookingDto(
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

    @Override
    public List<ParticipantDto> createParticipantsForBooking(Long bookingId, List<CreateParticipantCommand> createDtos) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> NotFoundException.forEntity("Booking", bookingId));

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
        for (CreateParticipantCommand dto : createDtos) {
            // Participant email is optional; fall back to booker email if not provided
            String participantEmail = dto.email();
            if (participantEmail != null) {
                participantEmail = participantEmail.trim();
            }
            if (participantEmail == null || participantEmail.isBlank()) {
                participantEmail = booking.getEmail() != null ? booking.getEmail().trim() : null;
            }

            Participant p = Participant.create(
                    booking.getEventId(),
                    booking.getId(),
                    booking.getBookingNumber(),
                    booking.getEmail(),
                    dto.firstName(),
                    dto.lastName(),
                    participantEmail
            );
            p.markCheckedIn(now);
            participants.save(p);

            CheckedInParticipant cip = new CheckedInParticipant();
            cip.setEventId(booking.getEventId());
            cip.setBookingId(booking.getId());
            cip.setBookingNumber(booking.getBookingNumber());
            cip.setFirstName(dto.firstName());
            cip.setLastName(dto.lastName());
            cip.setEmail(participantEmail);
            cip.setCheckedIn(true);
            cip.setCheckInTime(now);
            checkedInParticipants.save(cip);
        }

        return participants.findByBookingId(bookingId).stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getBookingNumber(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getBookerEmail(),
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipantDto> findParticipantsForBooking(Long bookingId) {
        activeBookings.findById(bookingId)
                .orElseThrow(() -> NotFoundException.forEntity("Booking", bookingId));
        return participants.findByBookingId(bookingId).stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getBookingNumber(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getBookerEmail(),
                        p.isCheckedIn(),
                        p.getCheckInTime()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto updateBookingCapacity(Long bookingId, int requestedSeats) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> NotFoundException.forEntity("Booking", bookingId));
        if (requestedSeats <= 0) {
            throw new IllegalArgumentException("Seat count must be greater than zero");
        }

        Long eventId = booking.getEventId();
        Event event = events.findById(eventId)
                .orElseThrow(() -> NotFoundException.forEntity("Event", eventId));
        int alreadyBookedOther = activeBookings.sumParticipantsByEventIdExcludingBooking(eventId, bookingId);
        int max = event.getMaxParticipants();
        int possible = max - alreadyBookedOther;
        if (requestedSeats > possible) {
            throw new IllegalArgumentException("Only " + possible + " seats available for this event.");
        }

        booking.setNumParticipants(requestedSeats);
        activeBookings.save(booking);

        long checked = participants.countByBookingIdAndCheckedInTrue(bookingId);
        return new BookingDto(
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

    @Override
    public NewBookingResponse createBookingForEvent(Long eventId, NewBookingRequestCommand request) {
        if (request == null) {
            throw new IllegalArgumentException("Missing booking data");
        }
        if (request.seats() <= 0) {
            throw new IllegalArgumentException("Seat count must be greater than zero");
        }
        Event event = events.findById(eventId)
                .orElseThrow(() -> NotFoundException.forEntity("Event", eventId));

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
        booking.setEventId(event.getEventId());
        booking.setNumParticipants(request.seats());
        booking.setFirstName(request.firstName());
        booking.setLastName(request.lastName());
        booking.setEmail(request.email());
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());
        activeBookings.save(booking);

        event.setAvailableSlots(remaining - request.seats());
        events.save(event);

        BookingDto dto = new BookingDto(
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

    @Override
    public void deleteBooking(Long bookingId) {
        ActiveBooking booking = activeBookings.findById(bookingId)
                .orElseThrow(() -> NotFoundException.forEntity("Booking", bookingId));

        Event event = events.findById(booking.getEventId())
                .orElseThrow(() -> NotFoundException.forEntity("Event", booking.getEventId()));
        participants.deleteByBookingId(bookingId);
        checkedInParticipants.deleteByBookingId(bookingId);
        activeBookings.delete(booking);

        int alreadyBooked = activeBookings.sumParticipantsByEventId(event.getEventId());
        event.setAvailableSlots(event.getMaxParticipants() - alreadyBooked);
        events.save(event);
    }
}

