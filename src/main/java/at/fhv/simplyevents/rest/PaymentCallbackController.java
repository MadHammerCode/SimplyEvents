package at.fhv.simplyevents.rest;
import at.fhv.simplyevents.persistence.ActiveBookingRepository;
import at.fhv.simplyevents.domain.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentCallbackController {

    @Value("${mockpay.sharedSecret:DEV_SECRET_123}")
    private String secret;

    private final ActiveBookingRepository activeBookingRepo;

    public PaymentCallbackController(ActiveBookingRepository activeBookingRepo) {
        this.activeBookingRepo = activeBookingRepo;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam Long bookingId,
            @RequestParam String paymentId,
            @RequestParam String status,
            @RequestParam String sig
    ) {
        // Signature check (einfach, fürs Schulprojekt ok)
        String expected = Integer.toHexString((paymentId + ":" + bookingId + ":" + secret).hashCode());
        if (!expected.equals(sig)) return ResponseEntity.status(403).build();

        var activeBooking = activeBookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ActiveBooking not found: " + bookingId
                ));

        if ("PAID".equalsIgnoreCase(status)) {
            activeBooking.setStatus(Status.PAID.name());
        } else if ("REFUNDED".equalsIgnoreCase(status)) {
            activeBooking.setStatus(Status.REFUNDED.name());
        } else {
            activeBooking.setStatus(Status.PAYMENT_FAILED.name());
        }

        // Note: ActiveBooking in this project does not seem to have a paymentReference column.
        // If you later add it, you can store `paymentId` here.

        activeBookingRepo.save(activeBooking);

        // zurück ins Frontend
        Long eventId = activeBooking.getEvent().getEventId();

        String redirectUrl = String.format(
                "/booking?eventId=%d&bookingId=%d&status=%s&paymentRef=%s",
                eventId,
                bookingId,
                status,
                paymentId
        );

        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }
}
