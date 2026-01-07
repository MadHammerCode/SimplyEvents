package at.fhv.simplyevents.domain.model;

import at.fhv.simplyevents.domain.DomainValidationException;
import java.time.Instant;
import java.util.Objects;

public class Participant {
    private Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private boolean checkedIn;
    private Instant checkInTime;
    private final Long eventId;
    private final Long bookingId;
    private final String bookingNumber;
    private final String bookerEmail;

    private Participant(Long id,
                        String firstName,
                        String lastName,
                        String email,
                        boolean checkedIn,
                        Instant checkInTime,
                        Long eventId,
                        Long bookingId,
                        String bookingNumber,
                        String bookerEmail) {
        require(eventId != null, "eventId is required");
        require(bookingId != null, "bookingId is required");
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.checkedIn = checkedIn;
        this.checkInTime = checkInTime;
        this.eventId = eventId;
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.bookerEmail = bookerEmail;
    }

    public static Participant create(Long eventId,
                                     Long bookingId,
                                     String bookingNumber,
                                     String bookerEmail,
                                     String firstName,
                                     String lastName,
                                     String email) {
        return new Participant(null, firstName, lastName, email, false, null, eventId, bookingId, bookingNumber, bookerEmail);
    }

    public static Participant restore(Long id,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      boolean checkedIn,
                                      Instant checkInTime,
                                      Long eventId,
                                      Long bookingId,
                                      String bookingNumber,
                                      String bookerEmail) {
        return new Participant(id, firstName, lastName, email, checkedIn, checkInTime, eventId, bookingId, bookingNumber, bookerEmail);
    }

    private static void require(boolean expression, String message) {
        if (!expression) {
            throw new DomainValidationException(message);
        }
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public boolean isCheckedIn() { return checkedIn; }
    public Instant getCheckInTime() { return checkInTime; }
    public Long getEventId() { return eventId; }
    public Long getBookingId() { return bookingId; }
    public String getBookingNumber() { return bookingNumber; }
    public String getBookerEmail() { return bookerEmail; }

    public void markCheckedIn(Instant at) {
        this.checkedIn = true;
        this.checkInTime = at == null ? Instant.now() : at;
    }

    public void markCheckedOut() {
        this.checkedIn = false;
        this.checkInTime = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participant)) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
