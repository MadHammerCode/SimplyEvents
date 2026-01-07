package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "participant")
public class ParticipantJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn;

    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "booking_number")
    private String bookingNumber;

    @Column(name = "booker_email")
    private String bookerEmail;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public Instant getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Instant checkInTime) { this.checkInTime = checkInTime; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public String getBookerEmail() { return bookerEmail; }
    public void setBookerEmail(String bookerEmail) { this.bookerEmail = bookerEmail; }
}

