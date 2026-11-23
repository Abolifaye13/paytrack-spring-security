
package com.example.paytrack.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceLineCalculatorTest {

    @Test
    void shouldCalculateLineTotalAfterDiscount() {

        // Arrange
        BigDecimal quantity = new BigDecimal("2");
        BigDecimal unitPrice = new BigDecimal("500.00");
        BigDecimal discount = new BigDecimal("100.00");

        // Act
        InvoiceLineCalculator calculator =
                new InvoiceLineCalculator();

        BigDecimal actualTotal = calculator.calculateLineTotal(
                quantity,
                unitPrice,
                discount
        );

        // Assert
        assertEquals(
                new BigDecimal("900.00"),
                actualTotal
        );
    }

@Test
void shouldCalculateTwoInvoiceLinesCorrectly() {

    InvoiceLineCalculator calculator =
            new InvoiceLineCalculator();

    BigDecimal firstLine = calculator.calculateLineTotal(
            new BigDecimal("2"),
            new BigDecimal("500.00"),
            new BigDecimal("100.00")
    );

    BigDecimal secondLine = calculator.calculateLineTotal(
            new BigDecimal("3"),
            new BigDecimal("200.00"),
            new BigDecimal("50.00")
    );

    BigDecimal invoiceTotal = firstLine.add(secondLine);

    assertEquals(
            new BigDecimal("1450.00"),
            invoiceTotal
    );
}

}
