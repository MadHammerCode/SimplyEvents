package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.EventUseCase;
import at.fhv.simplyevents.application.port.in.EventUseCase.CreateEventCommand;
import at.fhv.simplyevents.application.port.in.EventUseCase.EventResult;
import at.fhv.simplyevents.application.port.in.EventUseCase.ImageUpload;
import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.rest.dto.EventDtos.CreateEventRequest;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventUseCase eventUseCase;

    public EventRestController(EventUseCase eventUseCase) {
        this.eventUseCase = eventUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEventJson(
            @Valid @RequestBody CreateEventRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EventResult result = eventUseCase.createEvent(toCommand(request));
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEventMultipart(
            @Valid @RequestPart("event") CreateEventRequest request,
            BindingResult bindingResult,
            @RequestPart(value = "file", required = false) MultipartFile imageFile
    ) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EventResult result = eventUseCase.createEventWithImage(toCommand(request), toImage(imageFile));
            return ResponseEntity.ok(toResponse(result));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public List<EventResponse> getPublicEvents() {
        return eventUseCase.getPublicEvents().stream().map(this::toResponse).toList();
    }

    @GetMapping("/backoffice")
    public List<EventResponse> getBackofficeEvents(@RequestParam(required = false) EventStatus status) {
        if (status == null) {
            return eventUseCase.getAllEvents().stream().map(this::toResponse).toList();
        }
        return eventUseCase.getEventsByStatus(status).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toResponse(eventUseCase.getEventById(id)));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toResponse(eventUseCase.publishEvent(id)));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateEventJson(
            @PathVariable Long id,
            @Valid @RequestBody CreateEventRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EventResult result = eventUseCase.updateEvent(id, toCommand(request));
            return ResponseEntity.ok(toResponse(result));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateEventMultipart(
            @PathVariable Long id,
            @Valid @RequestPart("event") CreateEventRequest request,
            BindingResult bindingResult,
            @RequestPart(value = "file", required = false) MultipartFile imageFile
    ) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            EventResult result = eventUseCase.updateEventWithImage(id, toCommand(request), toImage(imageFile));
            return ResponseEntity.ok(toResponse(result));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            eventUseCase.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private CreateEventCommand toCommand(CreateEventRequest dto) {
        LocalDate date = dto.date() == null || dto.date().isBlank() ? null : LocalDate.parse(dto.date());
        LocalTime time = dto.time() == null || dto.time().isBlank() ? null : LocalTime.parse(dto.time());
        LocalDate bookingStart = dto.bookingStart() == null || dto.bookingStart().isBlank() ? null : LocalDate.parse(dto.bookingStart());
        LocalDate bookingEnd = dto.bookingEnd() == null || dto.bookingEnd().isBlank() ? null : LocalDate.parse(dto.bookingEnd());
        LocalDateTime cancellationDeadline = dto.cancellationDeadline() == null || dto.cancellationDeadline().isBlank()
                ? null : LocalDateTime.parse(dto.cancellationDeadline());
        return new CreateEventCommand(
                dto.title(),
                date,
                time,
                dto.location(),
                dto.price(),
                dto.minParticipants(),
                dto.maxParticipants(),
                dto.durationHours(),
                dto.category(),
                dto.description(),
                dto.equipmentNeeded(),
                dto.requirements(),
                cancellationDeadline,
                bookingStart,
                bookingEnd,
                dto.yearRound(),
                dto.confirmPast(),
                dto.capacity(),
                dto.imagePath(),
                dto.publishNow(),
                dto.status()
        );
    }

    private ImageUpload toImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return new ImageUpload(file.getOriginalFilename(), file.getBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read uploaded file.", e);
        }
    }

    private EventResponse toResponse(EventResult r) {
        return new EventResponse(
                r.id(),
                r.title(),
                r.date(),
                r.time(),
                r.location(),
                r.price(),
                r.minParticipants(),
                r.maxParticipants(),
                r.availableSlots(),
                r.durationHours(),
                r.capacity(),
                r.category(),
                r.description(),
                r.equipmentNeeded(),
                r.requirements(),
                r.cancellationDeadline(),
                r.imagePath(),
                r.bookingStart(),
                r.bookingEnd(),
                r.yearRound(),
                r.status()
        );
    }
}
