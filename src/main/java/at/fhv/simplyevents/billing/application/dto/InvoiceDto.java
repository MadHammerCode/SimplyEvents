package at.fhv.simplyevents.billing.application.dto;

import at.fhv.simplyevents.billing.domain.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for complete Invoice aggregate.
 */
public class InvoiceDto {

    private Long invoiceId;
    private Long eventId;
    private Long vendorId;
    private InvoiceStatus status;
    private String invoiceNumber;
    private BigDecimal total;
    private String currency;
    private String hash;
    private String pdfPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InvoiceLineDto> lines;
    private List<InvoiceShareDto> shares;

    public InvoiceDto() {
    }

    // Getters & Setters
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<InvoiceLineDto> getLines() { return lines; }
    public void setLines(List<InvoiceLineDto> lines) { this.lines = lines; }

    public List<InvoiceShareDto> getShares() { return shares; }
    public void setShares(List<InvoiceShareDto> shares) { this.shares = shares; }
}
