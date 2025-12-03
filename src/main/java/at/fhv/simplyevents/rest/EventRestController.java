package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.EventDtos.CreateEventRequest;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import at.fhv.simplyevents.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.persistence.EventRepository;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventService eventService;
    private final EventRepository eventRepository;

    public EventRestController(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody CreateEventRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/{eventId}/image")
    public ResponseEntity<Void> uploadEventImage(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir); // falls noch nicht existiert


        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFileName = "event_" + eventId + "_" + System.currentTimeMillis() + extension;

        Path targetPath = uploadDir.resolve(storedFileName);


        Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);


        event.setImagePath("uploads/" + storedFileName);
        eventRepository.save(event);

        return ResponseEntity.ok().build();
    }
}
