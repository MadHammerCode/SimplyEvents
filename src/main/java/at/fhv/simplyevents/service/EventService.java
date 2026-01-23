package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.EventUseCase;
import at.fhv.simplyevents.application.port.out.FileStoragePort;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.domain.repository.EventRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class EventService implements EventUseCase {

    private final EventRepositoryPort eventRepository;
    private final FileStoragePort fileStoragePort;
    private static final DateTimeFormatter BOOKING_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DEFAULT_IMAGE_PATH = "uploads/coming_soon.png";

    public EventService(EventRepositoryPort eventRepository, FileStoragePort fileStoragePort) {
        this.eventRepository = eventRepository;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public EventResult createEvent(CreateEventCommand command) {
        return createEventWithImage(command, null);
    }

    @Override
    public EventResult createEventWithImage(CreateEventCommand cmd, ImageUpload image) {
        boolean publishNow = Boolean.TRUE.equals(cmd.publishNow());
        boolean yearRound = Boolean.TRUE.equals(cmd.yearRound());

        // 1. Basic Validation (Always required)
        if (cmd.title() == null || cmd.title().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }

        // 2. Strict Validation (Only if Publishing)
        if (publishNow) {
            if (cmd.location() == null || cmd.location().isBlank()) {
                throw new IllegalArgumentException("Location is required to publish an event.");
            }
            if (cmd.category() == null || cmd.category().isBlank()) {
                throw new IllegalArgumentException("Category is required to publish an event.");
            }

            Integer rawCapacity = cmd.capacity() != null ? cmd.capacity() : cmd.maxParticipants();
            if (rawCapacity == null || rawCapacity < 1) {
                throw new IllegalArgumentException("Capacity must be at least 1 to publish an event.");
            }

            boolean hasDate = cmd.date() != null;
            boolean hasTime = cmd.time() != null;
            boolean bookingWindowProvided = cmd.bookingStart() != null && cmd.bookingEnd() != null;

            if (!hasDate || !hasTime) {
                if (!bookingWindowProvided && !yearRound) {
                    throw new IllegalArgumentException("Provide date & time, booking window, or enable year-round availability to publish.");
                }
            }

            // Check past date logic only if publishing
            if (hasDate && hasTime) {
                LocalDateTime startDateTime = LocalDateTime.of(cmd.date(), cmd.time());
                LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
                if (startDateTime.isBefore(now)) {
                    Boolean confirmed = cmd.confirmPast();
                    if (confirmed == null || !confirmed) {
                        throw new IllegalArgumentException("The event is in the past. If you still want to publish it, confirm this.");
                    }
                }
            }
        }

        if (cmd.bookingStart() != null && cmd.bookingEnd() != null && cmd.bookingStart().isAfter(cmd.bookingEnd())) {
            throw new IllegalArgumentException("The booking window is invalid: 'Booking from' must be before or equal to 'Booking until'.");
        }

        // 3. Prepare Data
        LocalDateTime startDateTime = null;
        if (cmd.date() != null && cmd.time() != null) {
            startDateTime = LocalDateTime.of(cmd.date(), cmd.time());
        }

        Date startDate = null;
        if (startDateTime != null) {
            startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date cancellationDeadlineDate = null;
        if (cmd.cancellationDeadline() != null) {
            cancellationDeadlineDate = Date.from(cmd.cancellationDeadline().atZone(ZoneId.systemDefault()).toInstant());
        }

        Date bookingStartDate = null;
        if (cmd.bookingStart() != null) {
            bookingStartDate = Date.from(cmd.bookingStart().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (cmd.bookingEnd() != null) {
            bookingEndDate = Date.from(cmd.bookingEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Integer resolvedCapacity = (cmd.capacity() != null) ? cmd.capacity() : cmd.maxParticipants();
        if (resolvedCapacity == null) resolvedCapacity = 0;

        // 4. Create Event
        Event event = Event.createDraft();
        event.applyDetails(
                cmd.title(),
                cmd.category(),
                cmd.price() == null ? null : cmd.price().doubleValue(),
                cmd.minParticipants(),
                resolvedCapacity,
                cmd.requirements(),
                cmd.equipmentNeeded(),
                cmd.location(),
                cmd.durationHours(),
                startDate,
                resolvedCapacity, // initial available slots = capacity
                cmd.description(),
                cancellationDeadlineDate,
                yearRound,
                bookingStartDate,
                bookingEndDate,
                resolveImagePath(image, cmd.imagePath(), DEFAULT_IMAGE_PATH),
                publishNow ? EventStatus.PUBLISHED : EventStatus.PLANNED,
                false,
                cmd.time() // <--- Passed time
        );

        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    @Override
    public EventResult updateEvent(Long id, CreateEventCommand cmd) {
        return updateEventWithImage(id, cmd, null);
    }

    @Override
    public EventResult updateEventWithImage(Long id, CreateEventCommand cmd, ImageUpload image) {
        // 1. Fetch Event FIRST so we know its current status
        Event event = eventRepository.findById(id).orElseThrow(() -> NotFoundException.forEntity("Event", id));

        boolean publishNow = Boolean.TRUE.equals(cmd.publishNow());
        boolean isAlreadyPublished = event.getStatus() == EventStatus.PUBLISHED;
        boolean shouldValidateStrictly = publishNow || isAlreadyPublished;

        // 2. Strict Validation (Only if Published or Publishing)
        if (shouldValidateStrictly) {
            boolean dateAndTimeProvided = (cmd.date() != null && cmd.time() != null);
            boolean yearRound = Boolean.TRUE.equals(cmd.yearRound());
            boolean bookingWindowProvided = (cmd.bookingStart() != null && cmd.bookingEnd() != null);

            if (!dateAndTimeProvided && !yearRound && !bookingWindowProvided) {
                throw new IllegalArgumentException("Please enter either date + time, or activate 'Available all year', or fill in both 'Booking from' and 'Booking until'.");
            }

            // Validate capacity if publishing
            Integer providedCapacity = cmd.capacity() != null ? cmd.capacity() : cmd.maxParticipants();
            if (providedCapacity == null && event.getMaxParticipants() <= 0) {
                // Check logic: if it's already published, it should have capacity.
                // If we are updating, we check if the result would be valid.
            }
        }

        if (cmd.bookingStart() != null && cmd.bookingEnd() != null && cmd.bookingStart().isAfter(cmd.bookingEnd())) {
            throw new IllegalArgumentException("The booking window is invalid.");
        }

        // 3. Prepare Data
        LocalDateTime startDateTime = null;
        if (cmd.date() != null && cmd.time() != null) {
            startDateTime = LocalDateTime.of(cmd.date(), cmd.time());
        }

        Date startDate = null;
        if (startDateTime != null) {
            startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date cancellationDeadlineDate = null;
        if (cmd.cancellationDeadline() != null) {
            cancellationDeadlineDate = Date.from(cmd.cancellationDeadline().atZone(ZoneId.systemDefault()).toInstant());
        }

        Date bookingStartDate = null;
        if (cmd.bookingStart() != null) {
            bookingStartDate = Date.from(cmd.bookingStart().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (cmd.bookingEnd() != null) {
            bookingEndDate = Date.from(cmd.bookingEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Capacity Logic (Preserve logic from your previous file)
        int newMin = cmd.minParticipants() == null ? event.getMinParticipants() : cmd.minParticipants();

        Integer providedCapacity = cmd.capacity() != null ? cmd.capacity() : cmd.maxParticipants();
        Integer oldCapacity = event.getMaxParticipants();
        Integer oldAvailable = event.getAvailableSlots();
        Integer finalCapacity = providedCapacity != null ? providedCapacity : event.getMaxParticipants();

        Integer finalAvailable;
        if (providedCapacity != null) {
            if (oldAvailable == null) {
                finalAvailable = providedCapacity;
            } else {
                // Adjust available slots by the delta of capacity change
                int prevCap = oldCapacity == null ? providedCapacity : oldCapacity;
                int delta = providedCapacity - prevCap;
                finalAvailable = Math.max(0, oldAvailable + delta);
            }
        } else {
            finalAvailable = oldAvailable == null ? event.getMaxParticipants() : oldAvailable;
        }

        // Field mapping with fallbacks to existing values
        Integer currentDuration = event.getDurationHours();
        Integer finalDuration = cmd.durationHours() == null ? currentDuration : cmd.durationHours();

        // Use fallbacks for other fields
        String finalEquipment = cmd.equipmentNeeded() == null ? event.getEquipmentNeeded() : cmd.equipmentNeeded();
        String finalRequirements = cmd.requirements() == null ? event.getRequirements() : cmd.requirements();
        String finalDescription = cmd.description() == null ? event.getDescription() : cmd.description();

        // Price is safe because event.getPrice() is primitive double
        double finalPrice = cmd.price() == null ? event.getPrice() : cmd.price().doubleValue();

        String finalCategory = cmd.category() == null ? event.getCategory() : cmd.category();
        String finalLocation = cmd.location() == null ? event.getLocation() : cmd.location();
        String finalImagePath = resolveImagePath(image, cmd.imagePath(), event.getImagePath());

        // ...

        // Determine Status
        EventStatus finalStatus = event.getStatus();
        if (publishNow) {
            finalStatus = EventStatus.PUBLISHED;
        }

        event.applyDetails(
                cmd.title(),
                finalCategory,
                finalPrice,
                newMin,
                finalCapacity,
                finalRequirements,
                finalEquipment,
                finalLocation,
                finalDuration,
                startDate,
                finalAvailable,
                finalDescription,
                cancellationDeadlineDate,
                Boolean.TRUE.equals(cmd.yearRound()),
                bookingStartDate,
                bookingEndDate,
                finalImagePath,
                finalStatus,
                event.isCancelled(),
                cmd.time() // <--- Passed time
        );

        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    @Override
    public List<EventResult> getAllEvents() {
        return eventRepository.findAll().stream().map(this::toResult).toList();
    }

    @Override
    public List<EventResult> getEventsByStatus(EventStatus status) {
        if (status == null) return getAllEvents();
        return eventRepository.findByStatus(status).stream().map(this::toResult).toList();
    }

    @Override
    public EventResult getEventById(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Event", id));
        return toResult(event);
    }

    @Override
    public List<EventResult> getPublicEvents() {
        List<EventStatus> visibleStatuses = List.of(EventStatus.PUBLISHED, EventStatus.ACTIVE, EventStatus.FULL);
        return eventRepository.findByStatusIn(visibleStatuses)
                .stream()
                .filter(event -> !event.isCancelled())
                .map(this::toResult)
                .toList();
    }

    @Override
    public EventResult publishEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> NotFoundException.forEntity("Event", id));
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled events cannot be published.");
        }
        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) throw NotFoundException.forEntity("Event", id);
        Event event = eventRepository.findById(id).orElseThrow();
        if (event.getImagePath() != null && !event.getImagePath().equals(DEFAULT_IMAGE_PATH)) {
            fileStoragePort.delete(event.getImagePath());
        }
        eventRepository.deleteById(id);
    }

    @Override
    public EventResult toggleEventCanceled(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> NotFoundException.forEntity("Event", id));
        event.toggleCancelled();
        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    private EventResult toResult(Event event) {
        String date = null;
        String time = null;

        // Date String
        if (event.getDate() != null) {
            LocalDateTime ldt = LocalDateTime.ofInstant(event.getDate().toInstant(), ZoneId.systemDefault());
            date = ldt.toLocalDate().toString();
            // Fallback time if specific time field is null
            if (event.getTime() == null) {
                time = ldt.toLocalTime().toString().substring(0, 5);
            }
        }

        // Time String (Priority)
        if (event.getTime() != null) {
            time = event.getTime().toString().substring(0, 5); // HH:mm
        }

        String cancellationDeadline = null;
        if (event.getCancellationDeadline() != null) {
            cancellationDeadline = event.getCancellationDeadline().toString();
        }

        String bookingStart = null;
        if (event.getBookingStart() != null) {
            Instant instStart = Instant.ofEpochMilli(event.getBookingStart().getTime());
            LocalDate bs = instStart.atZone(ZoneId.systemDefault()).toLocalDate();
            bookingStart = bs.format(BOOKING_DATE_FMT);
        }

        String bookingEnd = null;
        if (event.getBookingEnd() != null) {
            Instant instEnd = Instant.ofEpochMilli(event.getBookingEnd().getTime());
            LocalDate be = instEnd.atZone(ZoneId.systemDefault()).toLocalDate();
            bookingEnd = be.format(BOOKING_DATE_FMT);
        }

        return new EventResult(
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
                event.getMaxParticipants(),
                event.getCategory(),
                event.getDescription(),
                event.getEquipmentNeeded(),
                event.getRequirements(),
                cancellationDeadline,
                event.getImagePath(),
                bookingStart,
                bookingEnd,
                event.isYearRound(),
                event.getStatus() == null ? EventStatus.PLANNED.name() : event.getStatus().name(),
                event.isCancelled()
        );
    }

    private String resolveImagePath(ImageUpload image, String cmdImagePath, String fallbackPath) {
        if (image != null && image.content() != null && image.content().length > 0) {
            return storeImage(image);
        }
        if (cmdImagePath != null && !cmdImagePath.isBlank()) {
            return cmdImagePath;
        }
        if (fallbackPath != null && !fallbackPath.isBlank()) {
            return fallbackPath;
        }
        return DEFAULT_IMAGE_PATH;
    }

    private String storeImage(ImageUpload image) {
        return fileStoragePort.store(image.content(), image.filename());
    }
}