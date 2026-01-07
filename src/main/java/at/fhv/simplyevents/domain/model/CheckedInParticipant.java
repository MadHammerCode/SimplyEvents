package at.fhv.simplyevents.domain.model;

import java.time.Instant;
import java.util.Objects;

public class CheckedInParticipant {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean checkedIn;
    private Instant checkInTime;
    private Long eventId;
    private Long bookingId;
    private String bookingNumber;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckedInParticipant)) return false;
        CheckedInParticipant that = (CheckedInParticipant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
