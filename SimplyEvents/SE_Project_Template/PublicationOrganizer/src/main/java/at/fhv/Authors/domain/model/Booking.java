package at.fhv.Authors.domain.model;

import at.fhv.Authors.enums.BookingType;
import at.fhv.Authors.enums.PaymentMethod;
import at.fhv.Authors.enums.Status;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Access(AccessType.FIELD)
public class Booking {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long bookingId;

    private BookingType bookingType;
    private Status status;
    private int numParticipants;
    private Date optionDate;
    private PaymentMethod paymentMethod;
    private double priceTotal;

    @OneToOne
    @JoinColumn(name = "event_Id")
    private Event event;

    protected Booking(){

    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public Date getOptionDate() {
        return optionDate;
    }

    public void setOptionDate(Date optionDate) {
        this.optionDate = optionDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(double priceTotal) {
        this.priceTotal = priceTotal;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
