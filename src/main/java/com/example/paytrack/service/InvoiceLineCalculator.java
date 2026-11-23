
package com.example.paytrack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceLineCalculator {

    // Rounding rule: HALF_UP to 2 decimal places
    // for invoice amounts.

    public BigDecimal calculateLineTotal(
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discount) {

        BigDecimal subtotal = quantity.multiply(unitPrice);

        return subtotal.subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
