package at.fhv.simplyevents.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ActiveBooking {

    private Long id;
    private String bookingNumber;
    private Long eventId;
    private Integer numParticipants;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bookingType;
    private LocalDate optionDate;
    private String paymentMethod;
    private BigDecimal priceTotal;
    private String status;
    private LocalDateTime createdAt;
    private LocalDate attendanceDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Integer getNumParticipants() { return numParticipants; }
    public void setNumParticipants(Integer numParticipants) { this.numParticipants = numParticipants; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public LocalDate getOptionDate() { return optionDate; }
    public void setOptionDate(LocalDate optionDate) { this.optionDate = optionDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getPriceTotal() { return priceTotal; }
    public void setPriceTotal(BigDecimal priceTotal) { this.priceTotal = priceTotal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActiveBooking)) return false;
        ActiveBooking that = (ActiveBooking) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
