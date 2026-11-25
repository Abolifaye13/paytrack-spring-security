
package com.example.paytrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class GstCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    public record TaxResult(
            BigDecimal cgst,
            BigDecimal sgst,
            BigDecimal igst) {
    }

    public TaxResult calculate(
            BigDecimal taxableAmount,
            BigDecimal gstRate,
            boolean sameState) {

        Objects.requireNonNull(taxableAmount, "Taxable amount is required");
        Objects.requireNonNull(gstRate, "GST rate is required");

        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Taxable amount cannot be negative");
        }

        if (gstRate.compareTo(BigDecimal.ZERO) < 0
                || gstRate.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException(
                    "GST rate must be between 0 and 100");
        }

        BigDecimal totalTax = taxableAmount
                .multiply(gstRate)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        if (sameState) {
            BigDecimal halfTax = totalTax.divide(
                    new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);

            BigDecimal otherHalf = totalTax.subtract(halfTax);

            return new TaxResult(halfTax, otherHalf, BigDecimal.ZERO.setScale(2));
        }

        return new TaxResult(
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2),
                totalTax);
    }
}
