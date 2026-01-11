package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "booking")
public class BookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "booking_type")
    private String bookingType;

    @Column(name = "status")
    private String status;

    @Column(name = "num_participants")
    private int numParticipants;

    @Column(name = "option_date")
    private Date optionDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "price_total")
    private double priceTotal;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getNumParticipants() { return numParticipants; }
    public void setNumParticipants(int numParticipants) { this.numParticipants = numParticipants; }
    public Date getOptionDate() { return optionDate; }
    public void setOptionDate(Date optionDate) { this.optionDate = optionDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getPriceTotal() { return priceTotal; }
    public void setPriceTotal(double priceTotal) { this.priceTotal = priceTotal; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

