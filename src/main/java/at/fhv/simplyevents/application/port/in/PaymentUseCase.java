package at.fhv.simplyevents.application.port.in;

public interface PaymentUseCase {
    StartResponse startPayment(Long bookingId);
    CallbackResult handleCallback(Long bookingId, String status, String paymentRef);

    record StartResponse(String payUrl) {}
    record CallbackResult(String redirectUrl) {}
}

