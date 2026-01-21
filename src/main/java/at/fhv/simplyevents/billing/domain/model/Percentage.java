package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;


public class Percentage implements Serializable {

    private final BigDecimal value;

    public Percentage(BigDecimal value) {
        if (value == null) {
            throw new InvoiceValidationException("Percentage value cannot be null");
        }
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvoiceValidationException(
                "Percentage must be between 0 and 100, got: " + value
            );
        }
        this.value = value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Percentage(double value) {
        this(BigDecimal.valueOf(value));
    }

    public BigDecimal getValue() {
        return value;
    }


    public BigDecimal toMultiplier() {
        return value.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Percentage that = (Percentage) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value + "%";
    }
}
