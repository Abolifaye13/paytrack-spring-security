
package com.example.paytrack.service;

import com.example.paytrack.entity.invoice.Invoice;
import com.example.paytrack.entity.invoice.InvoiceLine;
import com.example.paytrack.repository.InvoiceLineRepository;
import com.example.paytrack.repository.InvoiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final InvoiceLineCalculator calculator;
    private final GstCalculator gstCalculator;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.calculator = new InvoiceLineCalculator();
        this.gstCalculator = new GstCalculator();
    }

    @Transactional
    public Invoice createDraft(
            Invoice invoice,
            List<InvoiceLine> lines) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "Invoice details are required");
        }

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one invoice line is required");
        }

        if (invoice.getOrganisation() == null) {
            throw new IllegalArgumentException(
                    "Organisation is required");
        }

        BigDecimal retentionPercentage =
                invoice.getRetentionPercentage();

        if (retentionPercentage == null
                || retentionPercentage.compareTo(BigDecimal.ZERO) < 0
                || retentionPercentage.compareTo(
                        new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "Retention percentage must be between 0 and 100");
        }

        String invoiceCurrency = invoice.getCurrency();

        if (invoiceCurrency == null
                || invoiceCurrency.isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice currency is required");
        }

        String supplierState = invoice.getSupplierState();
        String customerState = invoice.getCustomerState();

        if (supplierState == null || supplierState.isBlank()
                || customerState == null || customerState.isBlank()) {

            throw new IllegalArgumentException(
                    "Supplier and customer states are required");
        }

        boolean sameState = supplierState.trim()
                .equalsIgnoreCase(customerState.trim());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;

        for (InvoiceLine line : lines) {

            if (line == null) {
                throw new IllegalArgumentException(
                        "Invoice line cannot be null");
            }

            BigDecimal quantity = line.getQuantity();
            BigDecimal unitPrice = line.getUnitPrice();
            BigDecimal discount = line.getDiscount();
            BigDecimal gstRate = line.getGstRate();

            if (quantity == null
                    || quantity.compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than zero");
            }

            if (unitPrice == null
                    || unitPrice.compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Unit price cannot be negative");
            }

            if (discount == null
                    || discount.compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Discount cannot be negative");
            }

            if (gstRate == null
                    || gstRate.compareTo(BigDecimal.ZERO) < 0
                    || gstRate.compareTo(
                            new BigDecimal("100")) > 0) {

                throw new IllegalArgumentException(
                        "GST rate must be between 0 and 100");
            }

            if (line.getCurrency() == null
                    || !invoiceCurrency.equalsIgnoreCase(
                            line.getCurrency())) {

                throw new IllegalArgumentException(
                        "Invoice line currency must match invoice currency");
            }

            BigDecimal grossAmount =
                    quantity.multiply(unitPrice);

            if (discount.compareTo(grossAmount) > 0) {
                throw new IllegalArgumentException(
                        "Discount cannot exceed line amount");
            }

            // Recalculate line total on the server.
            // Ignore any total sent by the client.
            BigDecimal lineTotal =
                    calculator.calculateLineTotal(
                            quantity,
                            unitPrice,
                            discount);

            lineTotal = lineTotal.setScale(
                    2, RoundingMode.HALF_UP);

            line.setLineTotal(lineTotal);
            line.setInvoice(invoice);

            subtotal = subtotal.add(lineTotal);

            // Calculate GST separately for each invoice line.
            GstCalculator.TaxResult tax =
                    gstCalculator.calculate(
                            lineTotal,
                            gstRate,
                            sameState);

            cgstAmount = cgstAmount.add(tax.cgst());
            sgstAmount = sgstAmount.add(tax.sgst());
            igstAmount = igstAmount.add(tax.igst());
        }

        subtotal = subtotal.setScale(
                2, RoundingMode.HALF_UP);

        cgstAmount = cgstAmount.setScale(
                2, RoundingMode.HALF_UP);

        sgstAmount = sgstAmount.setScale(
                2, RoundingMode.HALF_UP);

        igstAmount = igstAmount.setScale(
                2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = cgstAmount
                .add(sgstAmount)
                .add(igstAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // Retention is calculated on subtotal before GST.
        BigDecimal retainedAmount = subtotal
                .multiply(retentionPercentage)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP);

        // Amount payable = subtotal + GST - retention.
        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .subtract(retainedAmount)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setCgstAmount(cgstAmount);
        invoice.setSgstAmount(sgstAmount);
        invoice.setIgstAmount(igstAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setRetainedAmount(retainedAmount);
        invoice.setReleasedAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("DRAFT");

        // Save invoice first.
        Invoice savedInvoice =
                invoiceRepository.save(invoice);

        // Save invoice lines with recalculated totals.
        for (InvoiceLine line : lines) {
            line.setInvoice(savedInvoice);
        }

        invoiceLineRepository.saveAll(lines);

        return savedInvoice;
    }
}
