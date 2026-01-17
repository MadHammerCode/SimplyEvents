package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.port.in.PaymentUseCase;
import at.fhv.simplyevents.application.port.in.PaymentUseCase.CallbackResult;
import at.fhv.simplyevents.application.port.in.PaymentUseCase.StartResponse;
import at.fhv.simplyevents.domain.model.Status;
import at.fhv.simplyevents.domain.repository.ActiveBookingRepositoryPort;
import at.fhv.simplyevents.application.port.in.BookingUseCase;
import at.fhv.simplyevents.application.port.in.EventUseCase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Service
public class PaymentService implements PaymentUseCase {

    @Value("${mockpay.baseUrl:http://localhost:8082}")
    private String mockPayBaseUrl;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String appBaseUrl;

    private final ActiveBookingRepositoryPort activeBookings;
    private final BookingUseCase bookingUseCase;
    private final EventUseCase eventUseCase;
    private final RestTemplate restTemplate;

    public PaymentService(ActiveBookingRepositoryPort activeBookings,
                          BookingUseCase bookingUseCase,
                          EventUseCase eventUseCase,
                          RestTemplateBuilder restTemplateBuilder) {
        this.activeBookings = activeBookings;
        this.bookingUseCase = bookingUseCase;
        this.eventUseCase = eventUseCase;
        this.restTemplate = restTemplateBuilder.build();
    }

    private record CreatePaymentRequest(
            Long bookingId,
            long amountCents,
            String amount,
            String currency,
            String successCallbackUrl,
            String cancelCallbackUrl
    ) {}

    private record CreatePaymentResponse(String paymentId, String payUrl) {}

    @Override
    public CallbackResult handleCallback(Long bookingId, String status, String paymentRef) {
        String normalized = status == null ? "PAID" : status.toUpperCase();

        var booking = bookingUseCase.getBooking(bookingId);
        if ("PAID".equalsIgnoreCase(normalized) || "SUCCESS".equalsIgnoreCase(normalized)) {
            booking.setStatus(Status.PAID.name());
        } else if ("FAILED".equalsIgnoreCase(normalized) || "PAYMENT_FAILED".equalsIgnoreCase(normalized)) {
            booking.setStatus(Status.PAYMENT_FAILED.name());
        } else {
            booking.setStatus(Status.PENDING_PAYMENT.name());
        }
        activeBookings.save(booking);

        String redirectUrl = appBaseUrl
                + "/booking?bookingId=" + bookingId
                + "&eventId=" + booking.getEventId()
                + "&status=" + URLEncoder.encode(normalized, StandardCharsets.UTF_8);
        if (paymentRef != null && !paymentRef.isBlank()) {
            redirectUrl += "&paymentRef=" + URLEncoder.encode(paymentRef, StandardCharsets.UTF_8);
        }

        return new CallbackResult(redirectUrl);
    }

    @Override
    public StartResponse startPayment(Long bookingId) {
        var booking = bookingUseCase.getBooking(bookingId);
        BigDecimal amount = resolveAmount(booking);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Payment amount must be greater than zero");
        }
        long amountCents = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        Long eventId = booking.getEventId();
        String successCallbackUrl = appBaseUrl + "/api/payments/callback?bookingId=" + bookingId + "&eventId=" + eventId + "&status=PAID";
        String cancelCallbackUrl = appBaseUrl + "/api/payments/callback?bookingId=" + bookingId + "&eventId=" + eventId + "&status=PAYMENT_FAILED";
        String createUrl = mockPayBaseUrl + "/api/payments/create";

        var req = new CreatePaymentRequest(
                bookingId,
                amountCents,
                amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "eur",
                successCallbackUrl,
                cancelCallbackUrl
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

        booking.setStatus(Status.PENDING_PAYMENT.name());
        activeBookings.save(booking);

        return new StartResponse(response.payUrl());
    }

    private BigDecimal resolveAmount(at.fhv.simplyevents.domain.model.ActiveBooking booking) {
        BigDecimal total = booking.getPriceTotal();
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            return total.setScale(2, RoundingMode.HALF_UP);
        }
        try {
            var event = eventUseCase.getEventById(booking.getEventId());
            BigDecimal unitPrice = event.price() == null ? BigDecimal.ZERO : event.price();
            int participants = booking.getNumParticipants() == null ? 1 : booking.getNumParticipants();
            return unitPrice.multiply(BigDecimal.valueOf(participants)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}

