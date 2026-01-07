package at.fhv.simplyevents.domain.model;

import at.fhv.simplyevents.domain.DomainValidationException;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Booking {

    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = Map.of(
            Status.PENDING, Set.of(Status.CONFIRMED, Status.PENDING_PAYMENT, Status.CANCELLED),
            Status.CONFIRMED, Set.of(Status.PENDING_PAYMENT, Status.CANCELLED),
            Status.PENDING_PAYMENT, Set.of(Status.PAID, Status.PAYMENT_FAILED, Status.CANCELLED),
            Status.PAID, Set.of(Status.REFUNDED, Status.CANCELLED),
            Status.PAYMENT_FAILED, Set.of(Status.PENDING_PAYMENT, Status.CANCELLED),
            Status.REFUNDED, Set.of(),
            Status.CANCELLED, Set.of()
    );

    private Long bookingId;
    private BookingType bookingType;
    private Status status;
    private int numParticipants;
    private Date optionDate;
    private PaymentMethod paymentMethod;
    private double priceTotal;
    private String paymentReference;
    private Long eventId;
    private Long userId;

    private Booking(Long bookingId,
                    BookingType bookingType,
                    Status status,
                    int numParticipants,
                    Date optionDate,
                    PaymentMethod paymentMethod,
                    double priceTotal,
                    String paymentReference,
                    Long eventId,
                    Long userId) {
        require(eventId != null, "eventId is required");
        require(userId != null, "userId is required");
        require(status != null, "status is required");
        require(numParticipants > 0, "numParticipants must be greater than zero");
        require(bookingType != null, "bookingType is required");
        require(priceTotal >= 0, "priceTotal cannot be negative");
        this.bookingId = bookingId;
        this.bookingType = bookingType;
        this.status = status;
        this.numParticipants = numParticipants;
        this.optionDate = optionDate;
        this.paymentMethod = paymentMethod;
        this.priceTotal = priceTotal;
        this.paymentReference = paymentReference;
        this.eventId = eventId;
        this.userId = userId;
    }

    public static Booking createNew(Long eventId,
                                    Long userId,
                                    BookingType bookingType,
                                    int numParticipants) {
        return new Booking(null, bookingType, Status.PENDING, numParticipants, null, null, 0d, null, eventId, userId);
    }

    public static Booking restore(Long bookingId,
                                  BookingType bookingType,
                                  Status status,
                                  int numParticipants,
                                  Date optionDate,
                                  PaymentMethod paymentMethod,
                                  double priceTotal,
                                  String paymentReference,
                                  Long eventId,
                                  Long userId) {
        return new Booking(bookingId, bookingType, status, numParticipants, optionDate, paymentMethod, priceTotal, paymentReference, eventId, userId);
    }

    private static void require(boolean expression, String message) {
        if (!expression) {
            throw new DomainValidationException(message);
        }
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public Status getStatus() {
        return status;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public Date getOptionDate() {
        return optionDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public double getPriceTotal() {
        return priceTotal;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public Long getEventId() { return eventId; }
    public Long getUserId() { return userId; }

    public void changeStatus(Status newStatus) {
        require(newStatus != null, "new status must not be null");
        if (this.status == null) {
            this.status = newStatus;
            return;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of()).contains(newStatus)) {
            throw new DomainValidationException("Invalid status transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public void updateParticipants(int newParticipantCount) {
        require(newParticipantCount > 0, "numParticipants must be greater than zero");
        this.numParticipants = newParticipantCount;
    }

    public void scheduleOptionUntil(Date optionDate) {
        this.optionDate = optionDate;
    }

    public void selectPaymentMethod(PaymentMethod method) {
        require(method != null, "paymentMethod is required");
        this.paymentMethod = method;
    }

    public void updatePriceTotal(double newPriceTotal) {
        require(newPriceTotal >= 0, "priceTotal cannot be negative");
        this.priceTotal = newPriceTotal;
    }

    public void recordPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingId, booking.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }
}
