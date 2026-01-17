package at.fhv.simplyevents.billing.infrastructure.persistence.model;

import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@Table(name = "invoice_share")
public class InvoiceShareJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @Column(nullable = false, length = 36)
    private String shareUuid; // Domain ID

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedAmount;

    // --- Constructors ---

    public InvoiceShareJpaEntity() {
    }

    public InvoiceShareJpaEntity(String shareUuid, Long userId, BigDecimal allocatedAmount) {
        this.shareUuid = shareUuid;
        this.userId = userId;
        this.allocatedAmount = allocatedAmount;
    }

    // --- Getters & Setters ---

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public String getShareUuid() {
        return shareUuid;
    }

    public void setShareUuid(String shareUuid) {
        this.shareUuid = shareUuid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }
}
