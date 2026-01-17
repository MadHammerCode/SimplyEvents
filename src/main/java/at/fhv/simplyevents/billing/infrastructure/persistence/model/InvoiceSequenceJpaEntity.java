package at.fhv.simplyevents.billing.infrastructure.persistence.model;

import jakarta.persistence.*;

@Entity
@Table(name = "invoice_sequence")
public class InvoiceSequenceJpaEntity {

    @Id
    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Long lastValue;

    // --- Constructors ---

    public InvoiceSequenceJpaEntity() {
    }

    public InvoiceSequenceJpaEntity(Integer year, Long lastValue) {
        this.year = year;
        this.lastValue = lastValue;
    }

    // --- Getters & Setters ---

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getLastValue() {
        return lastValue;
    }

    public void setLastValue(Long lastValue) {
        this.lastValue = lastValue;
    }

    public Long incrementAndGet() {
        this.lastValue++;
        return this.lastValue;
    }
}
