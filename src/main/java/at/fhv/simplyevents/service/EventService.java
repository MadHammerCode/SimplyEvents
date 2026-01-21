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
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

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

        if (cmd.title() == null || cmd.title().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (publishNow && (cmd.location() == null || cmd.location().isBlank())) {
            throw new IllegalArgumentException("Location is required to publish an event.");
        }
        if (publishNow && (cmd.category() == null || cmd.category().isBlank())) {
            throw new IllegalArgumentException("Category is required to publish an event.");
        }

        Integer rawCapacity = cmd.capacity() != null ? cmd.capacity() : cmd.maxParticipants();
        if (publishNow && (rawCapacity == null || rawCapacity < 1)) {
            throw new IllegalArgumentException("Capacity must be at least 1 to publish an event.");
        }
        int resolvedCapacity = rawCapacity == null ? 0 : Math.max(0, rawCapacity);

        boolean hasDate = cmd.date() != null;
        boolean hasTime = cmd.time() != null;
        boolean bookingWindowProvided = cmd.bookingStart() != null && cmd.bookingEnd() != null;

        LocalDateTime startDateTime = null;
        if (hasDate && hasTime) {
            startDateTime = LocalDateTime.of(cmd.date(), cmd.time());
        }

        if (publishNow) {
            if (!hasDate || !hasTime) {
                if (!bookingWindowProvided && !yearRound) {
                    throw new IllegalArgumentException("Provide date & time, booking window, or enable year-round availability to publish.");
                }
            }
        }

        if (bookingWindowProvided && cmd.bookingStart().isAfter(cmd.bookingEnd())) {
            throw new IllegalArgumentException("The booking window is invalid: 'Booking from' must be before or equal to 'Booking until'.");
        }

        if (publishNow && startDateTime != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            if (startDateTime.isBefore(now)) {
                Boolean confirmed = cmd.confirmPast();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("The event is in the past. If you still want to publish it, confirm this.");
                }
            }
        }

        LocalDateTime cancellationDeadline = cmd.cancellationDeadline();

        Date startDate = null;
        if (startDateTime != null) {
            startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date cancellationDeadlineDate = null;
        if (cancellationDeadline != null) {
            cancellationDeadlineDate = Date.from(cancellationDeadline.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date bookingStartDate = null;
        if (cmd.bookingStart() != null) {
            bookingStartDate = Date.from(cmd.bookingStart().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (cmd.bookingEnd() != null) {
            bookingEndDate = Date.from(cmd.bookingEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

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
                resolvedCapacity,
                cmd.description(),
                cancellationDeadlineDate,
                yearRound,
                bookingStartDate,
                bookingEndDate,
                resolveImagePath(image, cmd.imagePath(), DEFAULT_IMAGE_PATH),
                publishNow ? EventStatus.PUBLISHED : EventStatus.PLANNED,
                false
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
        LocalDateTime startDateTime = null;
        if (cmd.date() != null && cmd.time() != null) {
            startDateTime = LocalDateTime.of(cmd.date(), cmd.time());
        }

        boolean dateAndTimeProvided = startDateTime != null;
        boolean yearRound = Boolean.TRUE.equals(cmd.yearRound());
        boolean bookingWindowProvided = cmd.bookingStart() != null && cmd.bookingEnd() != null;

        if (!dateAndTimeProvided && !yearRound && !bookingWindowProvided) {
            throw new IllegalArgumentException("Please enter either date + time, or activate 'Available all year', or fill in both 'Booking from' and 'Booking until'.");
        }

        if (bookingWindowProvided && cmd.bookingStart().isAfter(cmd.bookingEnd())) {
            throw new IllegalArgumentException("The booking window is invalid: 'Booking from' must be before or equal to 'Booking until'.");
        }

        if (startDateTime != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            if (startDateTime.isBefore(now)) {
                Boolean confirmed = cmd.confirmPast();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("The event is in the past. If you still want to update it, confirm this.");
                }
            }
        }

        LocalDateTime cancellationDeadline = cmd.cancellationDeadline();

        Date startDate = null;
        if (startDateTime != null) {
            startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date cancellationDeadlineDate = null;
        if (cancellationDeadline != null) {
            cancellationDeadlineDate = Date.from(cancellationDeadline.atZone(ZoneId.systemDefault()).toInstant());
        }

        Date bookingStartDate = null;
        if (cmd.bookingStart() != null) {
            bookingStartDate = Date.from(cmd.bookingStart().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (cmd.bookingEnd() != null) {
            bookingEndDate = Date.from(cmd.bookingEnd().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Event event = eventRepository.findById(id).orElseThrow(() -> NotFoundException.forEntity("Event", id));

        int newMin = cmd.minParticipants() == null ? event.getMinParticipants() : cmd.minParticipants();
        int newMax = cmd.maxParticipants() == null ? event.getMaxParticipants() : cmd.maxParticipants();

        Integer providedCapacity = cmd.capacity() != null ? cmd.capacity() : (cmd.maxParticipants() == null ? null : cmd.maxParticipants());
        Integer oldCapacity = event.getMaxParticipants();
        Integer oldAvailable = event.getAvailableSlots();
        Integer finalCapacity = providedCapacity != null ? providedCapacity : event.getMaxParticipants();
        Integer finalAvailable;
        if (providedCapacity != null) {
            if (oldAvailable == null) {
                finalAvailable = providedCapacity;
            } else {
                int prevCap = oldCapacity == null ? providedCapacity : oldCapacity;
                int delta = providedCapacity - prevCap;
                int adjusted = Math.max(0, oldAvailable + delta);
                finalAvailable = adjusted;
            }
        } else {
            finalAvailable = oldAvailable == null ? event.getMaxParticipants() : oldAvailable;
        }

        int finalDuration = cmd.durationHours() == null ? event.getDurationHours() : cmd.durationHours();
        String finalEquipment = cmd.equipmentNeeded() == null ? event.getEquipmentNeeded() : cmd.equipmentNeeded();
        String finalRequirements = cmd.requirements() == null ? event.getRequirements() : cmd.requirements();
        String finalDescription = cmd.description() == null ? event.getDescription() : cmd.description();
        double finalPrice = cmd.price() == null ? event.getPrice() : cmd.price().doubleValue();
        String finalCategory = cmd.category() == null ? event.getCategory() : cmd.category();
        String finalLocation = cmd.location() == null ? event.getLocation() : cmd.location();
        String finalImagePath = resolveImagePath(image, cmd.imagePath(), event.getImagePath());

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
                event.getStatus(),
                event.isCancelled()
        );

        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    @Override
    public List<EventResult> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public List<EventResult> getEventsByStatus(EventStatus status) {
        if (status == null) {
            return getAllEvents();
        }
        return eventRepository.findByStatus(status)
                .stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public EventResult getEventById(Long id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Event", id));
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (!event.isYearRound()) {
            LocalDate start = null;
            LocalDate end = null;

            if (event.getBookingStart() != null) {
                Instant instStart = Instant.ofEpochMilli(event.getBookingStart().getTime());
                start = instStart.atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (event.getBookingEnd() != null) {
                Instant instEnd = Instant.ofEpochMilli(event.getBookingEnd().getTime());
                end = instEnd.atZone(ZoneId.systemDefault()).toLocalDate();
            }

            if (start != null && today.isBefore(start)) {
                throw new IllegalArgumentException("Booking period has not started yet.");
            }
            if (end != null && today.isAfter(end)) {
                throw new IllegalArgumentException("Booking period has ended.");
            }
        }
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
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Event", id));
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled events cannot be published.");
        }
        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);
        return toResult(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw NotFoundException.forEntity("Event", id);
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Event", id));

        String imagePath = event.getImagePath();
        if (imagePath != null && !imagePath.equals(DEFAULT_IMAGE_PATH) && !imagePath.isBlank()) {
            fileStoragePort.delete(imagePath);
        }

        eventRepository.deleteById(id);
    }

    @Override
    public EventResult toggleEventCanceled(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Event", id));
        event.toggleCancelled();
        Event saved = eventRepository.save(event);
        return toResult(saved);
    }


    private EventResult toResult(Event event) {
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

        Integer capacity = event.getMaxParticipants();

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
                capacity,
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
