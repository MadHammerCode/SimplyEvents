package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.persistence.EventRepository;
import at.fhv.simplyevents.rest.dto.EventDtos.CreateEventRequest;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Date;
import java.time.ZoneId;

import java.util.NoSuchElementException;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(CreateEventRequest dto) {

        LocalDateTime startDateTime = null;
        if (dto.date() != null && dto.time() != null && !dto.date().isBlank() && !dto.time().isBlank()) {
            LocalDate date = LocalDate.parse(dto.date());
            LocalTime time = LocalTime.parse(dto.time());
            startDateTime = LocalDateTime.of(date, time);
        }

        LocalDateTime cancellationDeadline = null;
        if (dto.cancellationDeadline() != null && !dto.cancellationDeadline().isBlank()) {
            cancellationDeadline = LocalDateTime.parse(dto.cancellationDeadline());
        }


        Date startDate = null;
        if (startDateTime != null) {
            startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date cancellationDeadlineDate = null;
        if (cancellationDeadline != null) {
            cancellationDeadlineDate = Date.from(cancellationDeadline.atZone(ZoneId.systemDefault()).toInstant());
        }

        Event event = new Event();
        event.setTitle(dto.title());
        event.setDate(startDate);
        event.setLocation(dto.location());

        if (dto.price() != null) {
            event.setPrice(dto.price().doubleValue());
        }

        event.setDescription(dto.description());
        event.setCategory(dto.category());
        event.setMinParticipants(dto.minParticipants());
        event.setMaxParticipants(dto.maxParticipants());
        event.setAvailableSlots(dto.maxParticipants());
        event.setDurationHours(dto.durationHours());
        event.setEquipmentNeeded(dto.equipmentNeeded());
        event.setRequirements(dto.requirements());
        event.setCancellationDeadline(cancellationDeadlineDate);

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEventById(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id " + id));
        return toResponse(event);
    }

    private EventResponse toResponse(Event event) {
        String date = null;
        String time = null;
        if (event.getDate() != null) {
            LocalDateTime ldt = LocalDateTime.ofInstant(event.getDate().toInstant(), ZoneId.systemDefault());
            date = ldt.toLocalDate().toString();
            time = ldt.toLocalTime().toString().substring(0, 5);
        }

        String cancellationDeadline = null;
        if (event.getCancellationDeadline() != null) {
            cancellationDeadline = event.getCancellationDeadline().toString();
        }

        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                date,
                time,
                event.getLocation(),
                java.math.BigDecimal.valueOf(event.getPrice()),
                event.getMinParticipants(),
                event.getMaxParticipants(),
                event.getAvailableSlots(),
                event.getDurationHours(),
                event.getCategory(),
                event.getDescription(),
                event.getEquipmentNeeded(),
                event.getRequirements(),
                cancellationDeadline
        );
    }
}