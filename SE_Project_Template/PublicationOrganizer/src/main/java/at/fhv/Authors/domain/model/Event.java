package at.fhv.Authors.domain.model;

import at.fhv.Authors.enums.BookingType;
import at.fhv.Authors.enums.PaymentMethod;
import at.fhv.Authors.enums.Status;
import jakarta.persistence.*;
import org.springframework.web.bind.annotation.Mapping;

import java.time.Period;
import java.util.*;

@Entity
@Access(AccessType.FIELD)
public class Event {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long eventId;

    private String title;
    private String category;
    private double price;
    private int minParticipants;
    private int maxParticipants;
    private String requirements;
    private String equipmentNeeded;
    private String location;
    private int durationHours;
    private Date date;
    private int availableSlots;
    private String description;
    private Date cancellationDeadline;

    @OneToOne(mappedBy = "event")
    private Booking booking;

    protected Event(){

    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public void setMinParticipants(int minParticipants) {
        this.minParticipants = minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getEquipmentNeeded() {
        return equipmentNeeded;
    }

    public void setEquipmentNeeded(String equipmentNeeded) {
        this.equipmentNeeded = equipmentNeeded;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(Date cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
