
package com.example.paytrack.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GstCalculatorTest {

    private final GstCalculator calculator = new GstCalculator();

    @Test
    void shouldCalculateCgstAndSgstForSameState() {

        // Hand calculation:
        // Taxable amount = 10000.00
        // GST rate = 18%
        // Total GST = 10000 * 18 / 100 = 1800.00
        // CGST = 900.00
        // SGST = 900.00

        GstCalculator.TaxResult result = calculator.calculate(
                new BigDecimal("10000.00"),
                new BigDecimal("18.00"),
                true
        );

        assertEquals(new BigDecimal("900.00"), result.cgst());
        assertEquals(new BigDecimal("900.00"), result.sgst());
        assertEquals(new BigDecimal("0.00"), result.igst());
    }

    @Test
    void shouldCalculateIgstForOtherState() {

        // Hand calculation:
        // Taxable amount = 20000.00
        // GST rate = 18%
        // IGST = 20000 * 18 / 100 = 3600.00

        GstCalculator.TaxResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                new BigDecimal("18.00"),
                false
        );

        assertEquals(new BigDecimal("0.00"), result.cgst());
        assertEquals(new BigDecimal("0.00"), result.sgst());
        assertEquals(new BigDecimal("3600.00"), result.igst());
    }

    @Test
    void shouldCalculateMixedTaxRatesForSameState() {

        // Hand calculation:
        // Line 1: 1000 * 5% = 50.00 GST
        // Line 2: 2000 * 12% = 240.00 GST
        // Total GST = 290.00
        // CGST = 145.00
        // SGST = 145.00

        GstCalculator.TaxResult firstLine = calculator.calculate(
                new BigDecimal("1000.00"),
                new BigDecimal("5.00"),
                true
        );

        GstCalculator.TaxResult secondLine = calculator.calculate(
                new BigDecimal("2000.00"),
                new BigDecimal("12.00"),
                true
        );

        BigDecimal totalCgst = firstLine.cgst().add(secondLine.cgst());
        BigDecimal totalSgst = firstLine.sgst().add(secondLine.sgst());

        assertEquals(new BigDecimal("145.00"), totalCgst);
        assertEquals(new BigDecimal("145.00"), totalSgst);
    }
}
