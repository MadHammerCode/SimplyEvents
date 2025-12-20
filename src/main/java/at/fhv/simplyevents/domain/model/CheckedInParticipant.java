package at.fhv.simplyevents.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "checked_in_participant", indexes = {
        @Index(name = "idx_checked_in_participant_event", columnList = "event_id"),
        @Index(name = "idx_checked_in_participant_booking", columnList = "booking_id")
})
public class CheckedInParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean checkedIn;

    private Instant checkInTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private ActiveBooking booking;

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
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public ActiveBooking getBooking() { return booking; }
    public void setBooking(ActiveBooking booking) { this.booking = booking; }
}
