package at.fhv.simplyevents.persistence.model;

import at.fhv.simplyevents.domain.model.EventStatus;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "event")
public class EventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "available_slots")
    private Integer availableSlots;

    private String description;
    private Date cancellationDeadline;

    @Column(name = "image_path")
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "year_round")
    private Boolean yearRound;

    @Temporal(TemporalType.DATE)
    private Date bookingStart;

    @Temporal(TemporalType.DATE)
    private Date bookingEnd;

    @Column(name = "vendor_profile_id")
    private Long vendorProfileId;

    @Column(name = "booking_id")
    private Long bookingId;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getMinParticipants() { return minParticipants; }
    public void setMinParticipants(int minParticipants) { this.minParticipants = minParticipants; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getCancellationDeadline() { return cancellationDeadline; }
    public void setCancellationDeadline(Date cancellationDeadline) { this.cancellationDeadline = cancellationDeadline; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public Boolean getYearRound() { return yearRound; }
    public void setYearRound(Boolean yearRound) { this.yearRound = yearRound; }
    public Date getBookingStart() { return bookingStart; }
    public void setBookingStart(Date bookingStart) { this.bookingStart = bookingStart; }
    public Date getBookingEnd() { return bookingEnd; }
    public void setBookingEnd(Date bookingEnd) { this.bookingEnd = bookingEnd; }
    public Long getVendorProfileId() { return vendorProfileId; }
    public void setVendorProfileId(Long vendorProfileId) { this.vendorProfileId = vendorProfileId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}
