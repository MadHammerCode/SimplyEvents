package at.fhv.simplyevents.billing.domain.exception;


public class ImmutableInvoiceException extends InvoiceException {

    public ImmutableInvoiceException(String message) {
        super(message);
    }

    public ImmutableInvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

