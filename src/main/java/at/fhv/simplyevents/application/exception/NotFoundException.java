package at.fhv.simplyevents.application.exception;

/**
 * Application-level runtime exception indicating a requested resource was not found.
 * Provides helpers to standardize error messages across the application.
 */
public class NotFoundException extends RuntimeException {

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException forEntity(String entityName, Object identifier) {
        return new NotFoundException(entityName + " not found: " + identifier);
    }

    public static NotFoundException forEntityAndField(String entityName, String fieldName, Object identifier) {
        return new NotFoundException(entityName + " not found: " + fieldName + "=" + identifier);
    }

    public static NotFoundException withMessage(String message) {
        return new NotFoundException(message);
    }
}
