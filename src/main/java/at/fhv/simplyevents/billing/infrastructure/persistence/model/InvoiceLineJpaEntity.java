package at.fhv.simplyevents.billing.infrastructure.persistence.model;

import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@Table(name = "invoice_line")
public class InvoiceLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;

    @Column(nullable = false, length = 36)
    private String lineUuid; // Domain ID

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    // --- Constructors ---

    public InvoiceLineJpaEntity() {
    }

    public InvoiceLineJpaEntity(String lineUuid, String description, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.lineUuid = lineUuid;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    // --- Getters & Setters ---

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public String getLineUuid() {
        return lineUuid;
    }

    public void setLineUuid(String lineUuid) {
        this.lineUuid = lineUuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
