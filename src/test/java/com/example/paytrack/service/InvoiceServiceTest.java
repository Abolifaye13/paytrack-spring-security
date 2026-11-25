
package com.example.paytrack.service;

import com.example.paytrack.entity.Organisation;
import com.example.paytrack.entity.invoice.Invoice;
import com.example.paytrack.entity.invoice.InvoiceLine;
import com.example.paytrack.repository.InvoiceLineRepository;
import com.example.paytrack.repository.InvoiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    private InvoiceRepository invoiceRepository;
    private InvoiceLineRepository invoiceLineRepository;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {

        invoiceRepository = mock(InvoiceRepository.class);
        invoiceLineRepository = mock(InvoiceLineRepository.class);

        invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceLineRepository
        );

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));
    }

    @Test
    void shouldCalculateCgstAndSgstForSameStateInvoice() {

        Invoice invoice = createInvoice(
                "INV-TEST-101",
                "Maharashtra",
                "Maharashtra",
                "10"
        );

        InvoiceLine line = createLine(
                "1",
                "10000.00",
                "0.00",
                "18"
        );

        Invoice result = invoiceService.createDraft(
                invoice,
                List.of(line)
        );

        assertEquals(
                new BigDecimal("10000.00"),
                result.getSubtotal()
        );

        assertEquals(
                new BigDecimal("900.00"),
                result.getCgstAmount()
        );

        assertEquals(
                new BigDecimal("900.00"),
                result.getSgstAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.getIgstAmount()
        );

        assertEquals(
                new BigDecimal("1800.00"),
                result.getTaxAmount()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                result.getRetainedAmount()
        );

        assertEquals(
                new BigDecimal("10800.00"),
                result.getTotalAmount()
        );
    }

    @Test
    void shouldCalculateIgstForInterstateInvoice() {

        Invoice invoice = createInvoice(
                "INV-TEST-102",
                "Maharashtra",
                "Gujarat",
                "0"
        );

        InvoiceLine line = createLine(
                "1",
                "20000.00",
                "0.00",
                "18"
        );

        Invoice result = invoiceService.createDraft(
                invoice,
                List.of(line)
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.getCgstAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.getSgstAmount()
        );

        assertEquals(
                new BigDecimal("3600.00"),
                result.getIgstAmount()
        );

        assertEquals(
                new BigDecimal("3600.00"),
                result.getTaxAmount()
        );

        assertEquals(
                new BigDecimal("23600.00"),
                result.getTotalAmount()
        );
    }

    @Test
    void shouldCalculateMixedGstRatesCorrectly() {

        Invoice invoice = createInvoice(
                "INV-TEST-103",
                "Maharashtra",
                "Maharashtra",
                "0"
        );

        InvoiceLine firstLine = createLine(
                "1",
                "1000.00",
                "0.00",
                "5"
        );

        InvoiceLine secondLine = createLine(
                "1",
                "2000.00",
                "0.00",
                "12"
        );

        Invoice result = invoiceService.createDraft(
                invoice,
                List.of(firstLine, secondLine)
        );

        assertEquals(
                new BigDecimal("3000.00"),
                result.getSubtotal()
        );

        assertEquals(
                new BigDecimal("145.00"),
                result.getCgstAmount()
        );

        assertEquals(
                new BigDecimal("145.00"),
                result.getSgstAmount()
        );

        assertEquals(
                new BigDecimal("290.00"),
                result.getTaxAmount()
        );

        assertEquals(
                new BigDecimal("3290.00"),
                result.getTotalAmount()
        );
    }

    @Test
    void shouldIgnoreClientSentTotalsAndRecalculateOnServer() {

        Invoice invoice = createInvoice(
                "INV-TEST-104",
                "Maharashtra",
                "Maharashtra",
                "10"
        );

        // Fake totals sent by client.
        invoice.setSubtotal(new BigDecimal("999999.00"));
        invoice.setRetainedAmount(new BigDecimal("999999.00"));
        invoice.setTotalAmount(new BigDecimal("999999.00"));

        InvoiceLine line = createLine(
                "2",
                "500.00",
                "100.00",
                "18"
        );

        // Fake line total sent by client.
        line.setLineTotal(new BigDecimal("999999.00"));

        Invoice result = invoiceService.createDraft(
                invoice,
                List.of(line)
        );

        // Server recalculates line total:
        // 2 * 500 - 100 = 900
        assertEquals(
                new BigDecimal("900.00"),
                line.getLineTotal()
        );

        assertEquals(
                new BigDecimal("900.00"),
                result.getSubtotal()
        );

        assertEquals(
                new BigDecimal("90.00"),
                result.getRetainedAmount()
        );

        // GST = 162, payable = 900 + 162 - 90
        assertEquals(
                new BigDecimal("972.00"),
                result.getTotalAmount()
        );
    }

    private Invoice createInvoice(
            String invoiceNumber,
            String supplierState,
            String customerState,
            String retentionPercentage) {

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setOrganisation(new Organisation());
        invoice.setSupplierState(supplierState);
        invoice.setCustomerState(customerState);
        invoice.setRetentionPercentage(
                new BigDecimal(retentionPercentage));
        invoice.setCurrency("INR");

        return invoice;
    }

    private InvoiceLine createLine(
            String quantity,
            String unitPrice,
            String discount,
            String gstRate) {

        InvoiceLine line = new InvoiceLine();

        line.setQuantity(new BigDecimal(quantity));
        line.setUnitPrice(new BigDecimal(unitPrice));
        line.setDiscount(new BigDecimal(discount));
        line.setGstRate(new BigDecimal(gstRate));
        line.setCurrency("INR");

        return line;
    }
}
