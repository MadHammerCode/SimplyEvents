package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ActiveBooking")
public class ActiveBookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "booking_number", nullable = false, unique = true, length = 32)
    private String bookingNumber;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "num_participants", nullable = false)
    private Integer numParticipants;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "booking_type")
    private String bookingType;

    @Column(name = "option_date")
    private LocalDate optionDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "price_total")
    private BigDecimal priceTotal;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    // getters/setters
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
}
