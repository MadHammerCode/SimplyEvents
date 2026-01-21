package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


public class InvoiceShare implements Serializable {

    private final String id;
    private final Long userId;
    private Money allocatedAmount; // Fixed after finalize

    public InvoiceShare(Long userId, Money allocatedAmount) {
        if (userId == null || userId <= 0) {
            throw new InvoiceValidationException("UserId must be positive, got: " + userId);
        }
        if (allocatedAmount == null) {
            throw new InvoiceValidationException("Allocated amount cannot be null");
        }

        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.allocatedAmount = allocatedAmount;
    }

    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getAllocatedAmount() {
        return allocatedAmount;
    }


    protected void setAllocatedAmount(Money amount) {
        if (amount == null) {
            throw new InvoiceValidationException("Allocated amount cannot be null");
        }
        this.allocatedAmount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceShare that = (InvoiceShare) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InvoiceShare{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", allocatedAmount=" + allocatedAmount +
                '}';
    }
}
