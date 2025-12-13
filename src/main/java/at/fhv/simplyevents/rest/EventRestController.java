package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.EventDtos.CreateEventRequest;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import at.fhv.simplyevents.service.EventService;
import at.fhv.simplyevents.domain.model.EventStatus;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;

    public EventRestController(EventService eventService) {
        this.eventService = eventService;
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
            EventResponse response = eventService.createEvent(request);
            return ResponseEntity.ok(response);
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
            EventResponse response = eventService.createEvent(request, imageFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Public endpoint: only events visible for everyone (e.g. PUBLISHED / ACTIVE / FULL)
    @GetMapping
    public List<EventResponse> getPublicEvents() {
        return eventService.getPublicEvents();
    }

    // Backoffice endpoint: filter all events by status (e.g. PLANNED, PUBLISHED, ACTIVE, FULL, CANCELLED)
    @GetMapping("/backoffice")
    public List<EventResponse> getBackofficeEvents(@RequestParam(required = false) EventStatus status) {
        if (status == null) {
            return eventService.getAllEvents();
        }
        return eventService.getEventsByStatus(status);
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // Backoffice: publish an event so it becomes publicly visible
    @PostMapping("/{id}/publish")
    public EventResponse publishEvent(@PathVariable Long id) {
        return eventService.publishEvent(id);
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
            EventResponse response = eventService.updateEvent(id, request);
            return ResponseEntity.ok(response);
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
            EventResponse response = eventService.updateEvent(id, request, imageFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
