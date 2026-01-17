package at.fhv.simplyevents.application.port.in;

import at.fhv.simplyevents.domain.model.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface EventUseCase {
    EventResult createEvent(CreateEventCommand command);
    EventResult createEventWithImage(CreateEventCommand command, ImageUpload image);
    List<EventResult> getPublicEvents();
    List<EventResult> getAllEvents();
    List<EventResult> getEventsByStatus(EventStatus status);
    EventResult getEventById(Long id);
    EventResult publishEvent(Long id);
    EventResult updateEvent(Long id, CreateEventCommand command);
    EventResult updateEventWithImage(Long id, CreateEventCommand command, ImageUpload image);
    void deleteEvent(Long id);

    record CreateEventCommand(
            String title,
            LocalDate date,
            LocalTime time,
            String location,
            BigDecimal price,
            Integer minParticipants,
            Integer maxParticipants,
            Integer durationHours,
            String category,
            String description,
            String equipmentNeeded,
            String requirements,
            LocalDateTime cancellationDeadline,
            LocalDate bookingStart,
            LocalDate bookingEnd,
            Boolean yearRound,
            Boolean confirmPast,
            Integer capacity,
            String imagePath,
            Boolean publishNow,
            String status
    ) {}

    record EventResult(
            Long id,
            String title,
            String date,
            String time,
            String location,
            BigDecimal price,
            Integer minParticipants,
            Integer maxParticipants,
            Integer availableSlots,
            Integer durationHours,
            Integer capacity,
            String category,
            String description,
            String equipmentNeeded,
            String requirements,
            String cancellationDeadline,
            String imagePath,
            String bookingStart,
            String bookingEnd,
            Boolean yearRound,
            String status
    ) {}

    record ImageUpload(String filename, byte[] content) {}
}
