package at.fhv.simplyevents.billing.interfaces.rest;

import at.fhv.simplyevents.billing.domain.exception.ImmutableInvoiceException;
import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class BillingExceptionHandler {


    @ExceptionHandler(ImmutableInvoiceException.class)
    public ResponseEntity<ErrorResponse> handleImmutableInvoice(ImmutableInvoiceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("IMMUTABLE_INVOICE", e.getMessage()));
    }

    @ExceptionHandler(InvoiceValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(InvoiceValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
    }

    @ExceptionHandler(InvoiceException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceException(InvoiceException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INVOICE_ERROR", e.getMessage()));
    }


    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
