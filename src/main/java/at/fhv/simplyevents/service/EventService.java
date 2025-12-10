package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.persistence.EventRepository;
import at.fhv.simplyevents.rest.dto.EventDtos.CreateEventRequest;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.Instant;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private static final DateTimeFormatter BOOKING_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String UPLOAD_DIR = "uploads";
    private static final String DEFAULT_IMAGE_PATH = UPLOAD_DIR + "/coming_soon.png";
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse createEvent(CreateEventRequest dto) {
        return createEvent(dto, null);
    }

    public EventResponse createEvent(CreateEventRequest dto, MultipartFile imageFile) {

        LocalDateTime startDateTime = null;
        if (dto.date() != null && dto.time() != null && !dto.date().isBlank() && !dto.time().isBlank()) {
            LocalDate date = LocalDate.parse(dto.date());
            LocalTime time = LocalTime.parse(dto.time());
            startDateTime = LocalDateTime.of(date, time);
        }

        // validate that at least one scheduling method is provided
        boolean dateAndTimeProvided = startDateTime != null;
        boolean yearRound = Boolean.TRUE.equals(dto.yearRound());
        boolean bookingWindowProvided = dto.bookingStart() != null && !dto.bookingStart().isBlank()
                && dto.bookingEnd() != null && !dto.bookingEnd().isBlank();

        if (!dateAndTimeProvided && !yearRound && !bookingWindowProvided) {
            throw new IllegalArgumentException("Please enter either date + time, or activate 'Available all year', or fill in both 'Booking from' and 'Booking until'.");
        }

        // if booking window provided, validate ordering
        if (bookingWindowProvided) {
            LocalDate bs = LocalDate.parse(dto.bookingStart());
            LocalDate be = LocalDate.parse(dto.bookingEnd());
            if (bs.isAfter(be)) {
                throw new IllegalArgumentException("The booking window is invalid: 'Booking from' must be before or equal to 'Booking until'.");
            }
        }

        // Server-side: if startDateTime is in the past and client didn't confirm, block creation
        if (startDateTime != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            if (startDateTime.isBefore(now)) {
                // if confirmPast is missing or false, reject
                Boolean confirmed = dto.confirmPast();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("The event is in the past. If you still want to create it, confirm this.");
                }
            }
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


        Date bookingStartDate = null;
        if (dto.bookingStart() != null && !dto.bookingStart().isBlank()) {
            LocalDate bs = LocalDate.parse(dto.bookingStart());
            bookingStartDate = Date.from(bs.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (dto.bookingEnd() != null && !dto.bookingEnd().isBlank()) {
            LocalDate be = LocalDate.parse(dto.bookingEnd());
            bookingEndDate = Date.from(be.atStartOfDay(ZoneId.systemDefault()).toInstant());
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

        // avoid NPEs when DTO integers are null -> provide safe defaults
        int minP = dto.minParticipants() == null ? 0 : dto.minParticipants();
        int maxP = dto.maxParticipants() == null ? 0 : dto.maxParticipants();
        event.setMinParticipants(minP);

        // Determine capacity: prefer explicit capacity field, otherwise fall back to maxParticipants
        Integer capacity = dto.capacity() != null ? dto.capacity() : (dto.maxParticipants() == null ? null : dto.maxParticipants());
        // If frontend sent 'capacity' prefer that as maxParticipants; otherwise use maxParticipants field
        if (capacity != null) {
            event.setMaxParticipants(capacity);
        } else {
            event.setMaxParticipants(maxP);
        }

        // Set availableSlots initially equal to capacity (or maxParticipants) as requested
        event.setAvailableSlots(capacity == null ? 0 : capacity);

        event.setDurationHours(dto.durationHours() == null ? 0 : dto.durationHours());

        event.setEquipmentNeeded(dto.equipmentNeeded());
        event.setRequirements(dto.requirements());
        event.setCancellationDeadline(cancellationDeadlineDate);

        event.setYearRound(Boolean.TRUE.equals(dto.yearRound()));
        event.setBookingStart(bookingStartDate);
        event.setBookingEnd(bookingEndDate);
        event.setImagePath(resolveImagePath(imageFile, dto.imagePath(), DEFAULT_IMAGE_PATH));

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public EventResponse updateEvent(Long id, CreateEventRequest dto) {
        return updateEvent(id, dto, null);
    }

    public EventResponse updateEvent(Long id, CreateEventRequest dto, MultipartFile imageFile) {
        // parse date/time if present
        LocalDateTime startDateTime = null;
        if (dto.date() != null && dto.time() != null && !dto.date().isBlank() && !dto.time().isBlank()) {
            LocalDate date = LocalDate.parse(dto.date());
            LocalTime time = LocalTime.parse(dto.time());
            startDateTime = LocalDateTime.of(date, time);
        }

        // validate scheduling
        boolean dateAndTimeProvided = startDateTime != null;
        boolean yearRound = Boolean.TRUE.equals(dto.yearRound());
        boolean bookingWindowProvided = dto.bookingStart() != null && !dto.bookingStart().isBlank()
                && dto.bookingEnd() != null && !dto.bookingEnd().isBlank();

        if (!dateAndTimeProvided && !yearRound && !bookingWindowProvided) {
            throw new IllegalArgumentException("Please enter either date + time, or activate 'Available all year', or fill in both 'Booking from' and 'Booking until'.");
        }

        if (bookingWindowProvided) {
            LocalDate bs = LocalDate.parse(dto.bookingStart());
            LocalDate be = LocalDate.parse(dto.bookingEnd());
            if (bs.isAfter(be)) {
                throw new IllegalArgumentException("The booking window is invalid: 'Booking from' must be before or equal to 'Booking until'.");
            }
        }

        // past date confirmation
        if (startDateTime != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            if (startDateTime.isBefore(now)) {
                Boolean confirmed = dto.confirmPast();
                if (confirmed == null || !confirmed) {
                    throw new IllegalArgumentException("The event is in the past. If you still want to update it, confirm this.");
                }
            }
        }

        // parse cancellation and booking dates
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

        Date bookingStartDate = null;
        if (dto.bookingStart() != null && !dto.bookingStart().isBlank()) {
            LocalDate bs = LocalDate.parse(dto.bookingStart());
            bookingStartDate = Date.from(bs.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date bookingEndDate = null;
        if (dto.bookingEnd() != null && !dto.bookingEnd().isBlank()) {
            LocalDate be = LocalDate.parse(dto.bookingEnd());
            bookingEndDate = Date.from(be.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // load existing event
        Event event = eventRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Event not found with id " + id));

        // update fields
        event.setTitle(dto.title());
        event.setDate(startDate);
        event.setLocation(dto.location());
        if (dto.price() != null) {
            event.setPrice(dto.price().doubleValue());
        }
        event.setDescription(dto.description());
        event.setCategory(dto.category());

        // safe updates to avoid NPEs
        int newMin = dto.minParticipants() == null ? event.getMinParticipants() : dto.minParticipants();
        int newMax = dto.maxParticipants() == null ? event.getMaxParticipants() : dto.maxParticipants();
        event.setMinParticipants(newMin);
        event.setMaxParticipants(newMax);

        // Handle capacity changes: prefer explicit capacity field; fallback to maxParticipants
        Integer providedCapacity = dto.capacity() != null ? dto.capacity() : (dto.maxParticipants() == null ? null : dto.maxParticipants());
        Integer oldCapacity = event.getMaxParticipants();
        Integer oldAvailable = event.getAvailableSlots();

        if (providedCapacity != null) {
            // If availableSlots is null, initialize it to providedCapacity
            if (oldAvailable == null) {
                event.setAvailableSlots(providedCapacity);
            } else {
                // compute delta between new capacity and previous capacity (use oldCapacity if present)
                int prevCap = oldCapacity == null ? providedCapacity : oldCapacity;
                int delta = providedCapacity - prevCap;
                int adjusted = Math.max(0, oldAvailable + delta);
                event.setAvailableSlots(adjusted);
            }
            // Also reflect the capacity in maxParticipants for backward compatibility
            event.setMaxParticipants(providedCapacity);
        } else {
            // no capacity provided: keep existing availableSlots
            event.setAvailableSlots(oldAvailable == null ? event.getMaxParticipants() : oldAvailable);
        }

        event.setDurationHours(dto.durationHours() == null ? event.getDurationHours() : dto.durationHours());

        event.setEquipmentNeeded(dto.equipmentNeeded());
        event.setRequirements(dto.requirements());
        event.setCancellationDeadline(cancellationDeadlineDate);
        event.setYearRound(Boolean.TRUE.equals(dto.yearRound()));
        event.setBookingStart(bookingStartDate);
        event.setBookingEnd(bookingEndDate);
        event.setImagePath(resolveImagePath(imageFile, dto.imagePath(), event.getImagePath()));

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
        // If capacity was explicitly stored in availableSlots originally, keep response capacity from maxParticipants

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
                capacity,
                event.getCategory(),
                event.getDescription(),
                event.getEquipmentNeeded(),
                event.getRequirements(),
                cancellationDeadline,
                event.getImagePath(),
                bookingStart,
                bookingEnd,
                event.isYearRound()
        );
    }

    private String resolveImagePath(MultipartFile uploadedFile, String dtoImagePath, String fallbackPath) {
        if (uploadedFile != null && !uploadedFile.isEmpty()) {
            return storeImage(uploadedFile);
        }
        if (dtoImagePath != null && !dtoImagePath.isBlank()) {
            return dtoImagePath;
        }
        if (fallbackPath != null && !fallbackPath.isBlank()) {
            return fallbackPath;
        }
        return DEFAULT_IMAGE_PATH;
    }

    private String storeImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image is too large. Max size is 5 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Image must have a valid extension.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image type. Allowed: jpg, jpeg, png, gif, webp.");
        }

        String storedFileName = "event_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path uploadDir = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return UPLOAD_DIR + "/" + storedFileName;
        } catch (IOException e) {
            throw new IllegalStateException("Could not store image file.", e);
        }
    }
}
