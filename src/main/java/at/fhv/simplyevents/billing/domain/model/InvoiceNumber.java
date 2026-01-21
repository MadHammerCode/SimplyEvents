package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.util.Objects;


public class InvoiceNumber implements Serializable {

    private final int year;
    private final long sequence;

    public InvoiceNumber(int year, long sequence) {
        if (year < 2000 || year > 2099) {
            throw new InvoiceValidationException("Year must be between 2000 and 2099, got: " + year);
        }
        if (sequence < 1 || sequence > 99999) {
            throw new InvoiceValidationException("Sequence must be between 1 and 99999, got: " + sequence);
        }
        this.year = year;
        this.sequence = sequence;
    }

    public int getYear() {
        return year;
    }

    public long getSequence() {
        return sequence;
    }


    public String format() {
        return String.format("%d-%05d", year, sequence);
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceNumber that = (InvoiceNumber) o;
        return year == that.year && sequence == that.sequence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, sequence);
    }
}
