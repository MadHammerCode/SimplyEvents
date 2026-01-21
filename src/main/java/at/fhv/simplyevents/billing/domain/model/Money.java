package at.fhv.simplyevents.billing.domain.model;

import at.fhv.simplyevents.billing.domain.exception.InvoiceValidationException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;


public class Money implements Serializable {

    public static final String DEFAULT_CURRENCY = "EUR";
    private static final int SCALE = 2;

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new InvoiceValidationException("Amount cannot be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new InvoiceValidationException("Currency cannot be null or blank");
        }
        if (amount.signum() < 0) {
            throw new InvoiceValidationException("Amount cannot be negative: " + amount);
        }

        // Enforce scale=2
        this.amount = amount.setScale(SCALE, java.math.RoundingMode.HALF_UP);
        this.currency = currency.toUpperCase();
    }

    public Money(BigDecimal amount) {
        this(amount, DEFAULT_CURRENCY);
    }

    public Money(double amount) {
        this(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }

    public Money(double amount, String currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvoiceValidationException(
                String.format("Cannot add %s to %s", other.currency, this.currency)
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvoiceValidationException(
                String.format("Cannot subtract %s from %s", other.currency, this.currency)
            );
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new InvoiceValidationException("Result would be negative");
        }
        return new Money(result, this.currency);
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public boolean isZero() {
        return this.amount.signum() == 0;
    }

    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvoiceValidationException(
                String.format("Cannot compare %s to %s", this.currency, other.currency)
            );
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InvoiceValidationException(
                String.format("Cannot compare %s to %s", this.currency, other.currency)
            );
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean equals(Money other, BigDecimal tolerance) {
        if (!this.currency.equals(other.currency)) {
            return false;
        }
        BigDecimal diff = this.amount.subtract(other.amount).abs();
        return diff.compareTo(tolerance) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
