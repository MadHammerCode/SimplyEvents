package at.fhv.simplyevents.domain;

/**
 * Raised when a domain invariant is violated inside a domain model.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

