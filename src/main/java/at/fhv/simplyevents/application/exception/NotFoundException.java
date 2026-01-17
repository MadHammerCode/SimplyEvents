package at.fhv.simplyevents.application.exception;


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
