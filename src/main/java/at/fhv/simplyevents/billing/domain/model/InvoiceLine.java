package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


public class InvoiceLine implements Serializable {

    private final String id;
    private final String description;
    private final int quantity;
    private final Money unitPrice;
    private final Money lineTotal;

    public InvoiceLine(String description, int quantity, Money unitPrice) {
        if (description == null || description.isBlank()) {
            throw new InvoiceValidationException("Description cannot be null or blank");
        }
        if (quantity <= 0) {
            throw new InvoiceValidationException("Quantity must be positive, got: " + quantity);
        }
        if (unitPrice == null) {
            throw new InvoiceValidationException("Unit price cannot be null");
        }

        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;

        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getLineTotal() {
        return lineTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceLine that = (InvoiceLine) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InvoiceLine{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
