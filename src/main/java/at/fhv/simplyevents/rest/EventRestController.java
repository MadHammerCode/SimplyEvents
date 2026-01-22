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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
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
        } catch (Exception ex) {
            throw ex;
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
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
        } catch (Exception ex) {
            throw ex;
        }
    }

    @GetMapping
    public List<EventResponse> getPublicEvents() {
        return eventUseCase.getPublicEvents().stream().map(this::toResponse).toList();
    }

    @GetMapping("/backoffice")
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
    public List<EventResponse> getBackofficeEvents(@RequestParam(required = false) EventStatus status) {
        if (status == null) {
            return eventUseCase.getAllEvents().stream().map(this::toResponse).toList();
        }
        return eventUseCase.getEventsByStatus(status).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        try {
            var result = eventUseCase.getEventById(id);
            // Blockiere Zugriff auf cancelled Events für öffentliche API
            if (Boolean.TRUE.equals(result.cancelled())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(toResponse(result));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toResponse(eventUseCase.publishEvent(id)));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-cancel")
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
    public ResponseEntity<EventResponse> toggleEventCanceled(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toResponse(eventUseCase.toggleEventCanceled(id)));
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
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
        } catch (Exception ex) {
            throw ex;
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
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
        } catch (Exception ex) {
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('BACKOFFICE', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        try {
            eventUseCase.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private CreateEventCommand toCommand(CreateEventRequest dto) {
        LocalDate date = parseDate(dto.date(), "Event date");
        LocalTime time = parseTime(dto.time(), "Event time");
        LocalDate bookingStart = parseDate(dto.bookingStart(), "Booking start date");
        LocalDate bookingEnd = parseDate(dto.bookingEnd(), "Booking end date");
        LocalDateTime cancellationDeadline = parseDateTime(dto.cancellationDeadline(), "Cancellation deadline");

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

    private LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " has invalid format. Expected: yyyy-MM-dd, got: " + dateStr, e);
        }
    }

    private LocalTime parseTime(String timeStr, String fieldName) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " has invalid format. Expected: HH:mm[:ss], got: " + timeStr, e);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr, String fieldName) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " has invalid format. Expected: yyyy-MM-ddThh:mm[:ss], got: " + dateTimeStr, e);
        }
    }

    private ImageUpload toImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            byte[] content = file.getBytes();
            if (content.length == 0) {
                throw new IllegalArgumentException("Uploaded file is empty.");
            }
            return new ImageUpload(file.getOriginalFilename(), content);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Failed to read uploaded image file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
            throw new IllegalArgumentException(errorMsg, e);
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
                r.status(),
                r.cancelled()
        );
    }
}
