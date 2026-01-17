package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.ImmutableInvoiceException;
import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


public class Invoice implements Serializable {

    private final Long id;
    private final Long eventId;
    private final Long vendorId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private InvoiceStatus status;
    private InvoiceNumber invoiceNumber;
    private Money total;
    private String hash;
    private String pdfPath;

    private final List<InvoiceLine> lines;
    private final List<InvoiceShare> shares;


    public Invoice(Long id, Long eventId, Long vendorId, LocalDateTime createdAt) {
        if (id != null && id <= 0) {
            throw new InvoiceValidationException("ID must be positive");
        }
        if (eventId == null || eventId <= 0) {
            throw new InvoiceValidationException("Event ID must be positive");
        }
        if (vendorId == null || vendorId <= 0) {
            throw new InvoiceValidationException("Vendor ID must be positive");
        }
        if (createdAt == null) {
            throw new InvoiceValidationException("Created At cannot be null");
        }

        this.id = id;
        this.eventId = eventId;
        this.vendorId = vendorId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = InvoiceStatus.DRAFT;
        this.total = new Money(BigDecimal.ZERO);
        this.lines = new ArrayList<>();
        this.shares = new ArrayList<>();
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public InvoiceNumber getInvoiceNumber() {
        return invoiceNumber;
    }

    public Money getTotal() {
        return total;
    }

    public String getHash() {
        return hash;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public List<InvoiceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public List<InvoiceShare> getShares() {
        return Collections.unmodifiableList(shares);
    }



    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public void setInvoiceNumber(InvoiceNumber invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public void setLines(java.util.List<InvoiceLine> lines) {
        this.lines.clear();
        this.lines.addAll(lines);
        recalculateTotal();
    }

    public void setShares(java.util.List<InvoiceShare> shares) {
        this.shares.clear();
        this.shares.addAll(shares);
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Domain Methods ---


    public void addLine(String description, int quantity, Money unitPrice) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot add line to invoice with status: " + status
            );
        }

        InvoiceLine line = new InvoiceLine(description, quantity, unitPrice);
        lines.add(line);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }


    public void removeLine(String lineId) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot remove line from invoice with status: " + status
            );
        }

        lines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }


    public void allocateShareByAmount(Long userId, Money amount) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot allocate shares to invoice with status: " + status
            );
        }

        // Check if user already has a share
        boolean exists = shares.stream().anyMatch(s -> s.getUserId().equals(userId));
        if (exists) {
            throw new InvoiceValidationException(
                "User " + userId + " already has a share on this invoice"
            );
        }

        shares.add(new InvoiceShare(userId, amount));
        this.updatedAt = LocalDateTime.now();
    }


    public void allocateShareByPercentage(Long userId, Percentage percentage) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot allocate shares to invoice with status: " + status
            );
        }

        // Check if user already has a share
        boolean exists = shares.stream().anyMatch(s -> s.getUserId().equals(userId));
        if (exists) {
            throw new InvoiceValidationException(
                "User " + userId + " already has a share on this invoice"
            );
        }

        Money amount = total.multiply(percentage.toMultiplier());
        shares.add(new InvoiceShare(userId, amount));
        this.updatedAt = LocalDateTime.now();
    }


    public void removeShare(String shareId) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot remove share from invoice with status: " + status
            );
        }

        shares.removeIf(share -> share.getId().equals(shareId));
        this.updatedAt = LocalDateTime.now();
    }


    public void finalize(InvoiceNumber invoiceNumber, String hash, String pdfPath) {
        if (status != InvoiceStatus.DRAFT) {
            throw new ImmutableInvoiceException(
                "Cannot finalize invoice with status: " + status
            );
        }

        if (lines.isEmpty()) {
            throw new InvoiceValidationException("Invoice must have at least one line");
        }

        if (invoiceNumber == null) {
            throw new InvoiceValidationException("Invoice number cannot be null");
        }
        if (hash == null || hash.isBlank()) {
            throw new InvoiceValidationException("Hash cannot be null or blank");
        }
        if (pdfPath == null || pdfPath.isBlank()) {
            throw new InvoiceValidationException("PDF path cannot be null or blank");
        }

        // Validate shares sum
        if (!shares.isEmpty()) {
            validateSharesSum();
        }

        this.invoiceNumber = invoiceNumber;
        this.hash = hash;
        this.pdfPath = pdfPath;
        this.status = InvoiceStatus.FINAL;
        this.updatedAt = LocalDateTime.now();
    }


    private void validateSharesSum() {
        BigDecimal tolerance = BigDecimal.valueOf(0.01);

        Money shareSum = shares.stream()
            .map(InvoiceShare::getAllocatedAmount)
            .reduce(new Money(BigDecimal.ZERO), Money::add);

        if (!total.equals(shareSum, tolerance)) {
            throw new InvoiceValidationException(
                String.format(
                    "Share sum %.2f does not match invoice total %.2f",
                    shareSum.getAmount(), total.getAmount()
                )
            );
        }
    }


    private void recalculateTotal() {
        this.total = lines.stream()
            .map(InvoiceLine::getLineTotal)
            .reduce(new Money(BigDecimal.ZERO), Money::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", status=" + status +
                ", invoiceNumber=" + invoiceNumber +
                ", total=" + total +
                ", lineCount=" + lines.size() +
                ", shareCount=" + shares.size() +
                '}';
    }
}
