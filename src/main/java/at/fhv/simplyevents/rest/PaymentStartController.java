package at.fhv.simplyevents.rest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import at.fhv.simplyevents.persistence.ActiveBookingRepository;
import at.fhv.simplyevents.domain.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentStartController {

    @Value("${mockpay.baseUrl:http://localhost:8082}")
    private String mockPayBaseUrl;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String appBaseUrl;

    private final ActiveBookingRepository activeBookingRepo;
    private final RestTemplate restTemplate;

    public PaymentStartController(ActiveBookingRepository activeBookingRepo, RestTemplateBuilder restTemplateBuilder) {
        this.activeBookingRepo = activeBookingRepo;
        this.restTemplate = restTemplateBuilder.build();
    }

    public record StartResponse(String payUrl) {}

    public record CreatePaymentRequest(
            Long bookingId,
            long amountCents,
            String currency,
            String successCallbackUrl,
            String cancelCallbackUrl
    ) {}

    public record CreatePaymentResponse(String paymentId, String payUrl) {}

    @PostMapping("/start/{bookingId}")
    public StartResponse start(@PathVariable Long bookingId) {
        var activeBooking = activeBookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ActiveBooking not found: " + bookingId
                ));

        BigDecimal priceTotal = activeBooking.getPriceTotal();
        long amountCents = priceTotal
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String callbackUrl = appBaseUrl + "/api/payments/callback";
        String createUrl = mockPayBaseUrl + "/api/payments/create";

        var req = new CreatePaymentRequest(
                bookingId,
                amountCents,
                "eur",
                callbackUrl,
                callbackUrl
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.postForObject(
                createUrl,
                new HttpEntity<>(req, headers),
                CreatePaymentResponse.class
        );

        if (response == null || response.payUrl() == null) {
            throw new IllegalStateException("MockPay returned no payUrl");
        }

        String payUrl = response.payUrl();

        activeBooking.setStatus(Status.PENDING_PAYMENT.name());
        activeBookingRepo.save(activeBooking);

        return new StartResponse(payUrl);
    }
}
