package at.fhv.simplyevents.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PendingBookingCache {

    public record PendingBooking(
            String id,
            String bookingNumber,
            Long eventId,
            String firstName,
            String lastName,
            String email,
            String phone,
            int numParticipants,
            BigDecimal priceTotal,
            String paymentMethod,
            Instant createdAt,
            Instant expiresAt,
            Status state,
            LocalDate attendanceDate
    ) {
        public enum Status {
            CREATED,
            READY_FOR_PAYMENT,
            PAYMENT_IN_PROGRESS,
            COMPLETED,
            FAILED
        }
    }

    private final Map<String, PendingBooking> pending = new ConcurrentHashMap<>();
    private final Duration ttl;

    public PendingBookingCache(Duration ttl) {
        this.ttl = ttl;
    }

    public PendingBooking register(
            Long eventId,
            String firstName,
            String lastName,
            String email,
            String phone,
            int numParticipants,
            BigDecimal priceTotal,
            String bookingNumber,
            LocalDate attendanceDate
    ) {
        cleanupExpired();
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PendingBooking booking = new PendingBooking(
                id,
                bookingNumber,
                eventId,
                firstName,
                lastName,
                email,
                phone,
                numParticipants,
                priceTotal,
                null,
                now,
                now.plus(ttl),
                PendingBooking.Status.CREATED,
                attendanceDate
        );
        pending.put(id, booking);
        return booking;
    }

    public Optional<PendingBooking> get(String id) {
        cleanupExpired();
        return Optional.ofNullable(pending.get(id));
    }

    public void updateState(String id, PendingBooking.Status newState) {
        pending.computeIfPresent(id, (key, oldValue) ->
                new PendingBooking(
                        oldValue.id(),
                        oldValue.bookingNumber(),
                        oldValue.eventId(),
                        oldValue.firstName(),
                        oldValue.lastName(),
                        oldValue.email(),
                        oldValue.phone(),
                        oldValue.numParticipants(),
                        oldValue.priceTotal(),
                        oldValue.paymentMethod(),
                        oldValue.createdAt(),
                        oldValue.expiresAt(),
                        newState,
                        oldValue.attendanceDate()
                ));
    }

    public void updatePaymentMethod(String id, String newPaymentMethod, PendingBooking.Status newState) {
        pending.computeIfPresent(id, (key, oldValue) ->
                new PendingBooking(
                        oldValue.id(),
                        oldValue.bookingNumber(),
                        oldValue.eventId(),
                        oldValue.firstName(),
                        oldValue.lastName(),
                        oldValue.email(),
                        oldValue.phone(),
                        oldValue.numParticipants(),
                        oldValue.priceTotal(),
                        newPaymentMethod,
                        oldValue.createdAt(),
                        oldValue.expiresAt(),
                        newState,
                        oldValue.attendanceDate()
                ));
    }

    public Optional<PendingBooking> remove(String id) {
        return Optional.ofNullable(pending.remove(id));
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        pending.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
