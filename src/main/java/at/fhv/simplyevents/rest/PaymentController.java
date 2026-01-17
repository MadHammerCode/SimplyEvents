package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.port.in.PaymentUseCase;
import at.fhv.simplyevents.application.port.in.PaymentUseCase.StartResponse;
import at.fhv.simplyevents.application.port.in.PaymentUseCase.CallbackResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> handleCallback(@RequestParam(name = "bookingId") Long bookingId,
                                               @RequestParam(name = "status", required = false) String status,
                                               @RequestParam(name = "paymentRef", required = false) String paymentRef) {
        CallbackResult result = paymentUseCase.handleCallback(bookingId, status, paymentRef);
        return ResponseEntity.status(302)
                .header("Location", result.redirectUrl())
                .build();
    }

    @PostMapping("/start/{bookingId}")
    public StartResponse start(@PathVariable Long bookingId) {
        return paymentUseCase.startPayment(bookingId);
    }
}