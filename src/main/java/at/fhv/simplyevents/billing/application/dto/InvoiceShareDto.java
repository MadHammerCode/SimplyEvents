package at.fhv.simplyevents.billing.application.dto;

import java.math.BigDecimal;

/**
 * DTO for invoice share/allocation.
 */
public class InvoiceShareDto {

    private String id;
    private Long userId;
    private BigDecimal allocatedAmount;

    public InvoiceShareDto() {
    }

    public InvoiceShareDto(String id, Long userId, BigDecimal allocatedAmount) {
        this.id = id;
        this.userId = userId;
        this.allocatedAmount = allocatedAmount;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
}
