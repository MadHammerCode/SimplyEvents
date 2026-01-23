package at.fhv.simplyevents.domain.model;

import at.fhv.simplyevents.domain.DomainValidationException;

import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

public class Event {

    private Long eventId;
    private String title;
    private String category;
    private double price;
    private int minParticipants;
    private int maxParticipants;
    private String requirements;
    private String equipmentNeeded;
    private String location;
    private Integer durationHours;
    private Date date;
    private LocalTime time; // Ensure time is stored if used by controller
    private Integer availableSlots;
    private String description;
    private Date cancellationDeadline;
    // references to other aggregates by ID only
    private Long bookingId;
    private Boolean yearRound;
    private Date bookingStart;
    private Date bookingEnd;
    private Long vendorProfileId;
    private String imagePath;
    private EventStatus status;
    private Boolean cancelled;

    protected Event() {
        this.status = EventStatus.PLANNED;
        this.cancelled = false;
    }

    public static Event createDraft() {
        return new Event();
    }

    public void loadIdentifiers(Long eventId, Long vendorProfileId, Long bookingId) {
        this.eventId = eventId;
        this.vendorProfileId = vendorProfileId;
        this.bookingId = bookingId;
    }

    public void applyDetails(
            String title,
            String category,
            Double price,
            Integer minParticipants,
            Integer maxParticipants,
            String requirements,
            String equipmentNeeded,
            String location,
            Integer durationHours,
            Date date,
            Integer availableSlots,
            String description,
            Date cancellationDeadline,
            Boolean yearRound,
            Date bookingStart,
            Date bookingEnd,
            String imagePath,
            EventStatus status,
            Boolean cancelled,
            LocalTime time // <--- Added Argument (to match your Mapper)
    ) {
        // Pass status and relevant fields to validate
        validate(title, price, minParticipants, maxParticipants, availableSlots, bookingStart, bookingEnd, date, time, yearRound, status);

        this.title = title.trim();
        this.category = category;
        this.price = price == null ? 0 : price;
        this.minParticipants = minParticipants == null ? 0 : minParticipants;
        this.maxParticipants = maxParticipants == null ? 0 : maxParticipants;
        this.requirements = requirements;
        this.equipmentNeeded = equipmentNeeded;
        this.location = location;
        this.durationHours = durationHours;
        this.date = copy(date);
        this.time = time;
        setAvailableSlots(resolveAvailableSlots(availableSlots, this.maxParticipants));
        this.description = description;
        this.cancellationDeadline = copy(cancellationDeadline);
        this.yearRound = yearRound;
        this.bookingStart = copy(bookingStart);
        this.bookingEnd = copy(bookingEnd);
        this.imagePath = imagePath;
        this.status = status == null ? EventStatus.PLANNED : status;
        this.cancelled = cancelled == null ? false : cancelled;
    }

    private void validate(String title, Double price, Integer minParticipants, Integer maxParticipants, Integer availableSlots,
                          Date bookingStart, Date bookingEnd, Date date, LocalTime time, Boolean yearRound, EventStatus status) {

        // 1. Basic Identity (Always Required)
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("Title must not be empty.");
        }

        // 2. ESCAPE HATCH: If it is a draft (PLANNED), stop here.
        // We don't enforce prices, dates, or locations for drafts.
        if (status == EventStatus.PLANNED) {
            return;
        }

        // 3. PUBLISHED CHECKS (Strict rules only apply when publishing)
        if (price != null && price < 0) {
            throw new DomainValidationException("Price must not be negative.");
        }

        int min = minParticipants == null ? 0 : minParticipants;
        int max = maxParticipants == null ? 0 : maxParticipants;

        if (min < 0) throw new DomainValidationException("Min participants must not be negative.");
        if (max < min) throw new DomainValidationException("Max participants must be greater or equal to min participants.");

        if (availableSlots != null) {
            if (availableSlots < 0) throw new DomainValidationException("Available slots must not be negative.");
            if (max > 0 && availableSlots > max) throw new DomainValidationException("Available slots cannot exceed max participants.");
        }

        if (bookingStart != null && bookingEnd != null && bookingStart.after(bookingEnd)) {
            throw new DomainValidationException("Booking start must not be after booking end.");
        }

        // The Logic causing your 400 Error:
        boolean hasSpecificDate = (date != null); // If you have separate time, add: && time != null
        boolean isYearRound = Boolean.TRUE.equals(yearRound);
        boolean hasBookingWindow = (bookingStart != null && bookingEnd != null);

        if (!hasSpecificDate && !isYearRound && !hasBookingWindow) {
            throw new DomainValidationException("Please enter either date + time, or activate 'Available all year', or fill in both 'Booking from' and 'Booking until'.");
        }
    }

    private Integer resolveAvailableSlots(Integer availableSlots, int maxParticipants) {
        if (availableSlots == null) {
            return maxParticipants;
        }
        return availableSlots;
    }

    public void setAvailableSlots(Integer slots) {
        if (slots == null) {
            this.availableSlots = this.maxParticipants;
            return;
        }
        if (slots < 0) {
            throw new DomainValidationException("Available slots must not be negative.");
        }
        if (this.maxParticipants > 0 && slots > this.maxParticipants) {
            throw new DomainValidationException("Available slots cannot exceed max participants.");
        }
        this.availableSlots = slots;
    }

    public void setStatus(EventStatus status) {
        if (status == null) {
            throw new DomainValidationException("Status must not be null.");
        }
        this.status = status;
    }

    public void toggleCancelled() {
        this.cancelled = !Boolean.TRUE.equals(this.cancelled);
    }

    private Date copy(Date source) {
        return source == null ? null : new Date(source.getTime());
    }

    public Long getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getMinParticipants() { return minParticipants; }
    public int getMaxParticipants() { return maxParticipants; }
    public String getRequirements() { return requirements; }
    public String getEquipmentNeeded() { return equipmentNeeded; }
    public String getLocation() { return location; }
    public Integer getDurationHours() { return durationHours; }
    public Date getDate() { return copy(date); }
    public LocalTime getTime() { return time; }
    public Integer getAvailableSlots() { return availableSlots; }
    public String getDescription() { return description; }
    public Date getCancellationDeadline() { return copy(cancellationDeadline); }
    public Long getBookingId() { return bookingId; }
    public boolean isYearRound() { return Boolean.TRUE.equals(yearRound); }
    public Date getBookingStart() { return copy(bookingStart); }
    public Date getBookingEnd() { return copy(bookingEnd); }
    public Long getVendorProfileId() { return vendorProfileId; }
    public String getImagePath() { return imagePath; }
    public EventStatus getStatus() { return status; }
    public boolean isCancelled() { return Boolean.TRUE.equals(cancelled); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode() { return Objects.hash(eventId); }
}