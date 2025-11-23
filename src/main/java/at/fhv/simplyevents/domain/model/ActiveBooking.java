package at.fhv.simplyevents.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ActiveBooking")
public class ActiveBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    // Öffentliche Buchungsnummer für den Benutzer
    @Column(name = "booking_number", nullable = false, unique = true, length = 32)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "num_participants", nullable = false)
    private Integer numParticipants;

    // Persönliche Daten
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    // Optional: Felder, die du laut Log schon hast
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(Integer numParticipants) {
        this.numParticipants = numParticipants;
    }

    public LocalDate getOptionDate() {
        return optionDate;
    }

    public void setOptionDate(LocalDate optionDate) {
        this.optionDate = optionDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(BigDecimal priceTotal) {
        this.priceTotal = priceTotal;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
