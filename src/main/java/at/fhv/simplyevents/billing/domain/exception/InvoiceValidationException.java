package at.fhv.simplyevents.billing.domain.exception;


public class InvoiceValidationException extends InvoiceException {

    public InvoiceValidationException(String message) {
        super(message);
    }

    public InvoiceValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
