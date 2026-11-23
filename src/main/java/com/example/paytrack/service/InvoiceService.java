
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

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final InvoiceLineCalculator calculator;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.calculator = new InvoiceLineCalculator();
    }

    @Transactional
    public Invoice createDraft(Invoice invoice, List<InvoiceLine> lines) {

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invoice must contain at least one line");
        }

        if (invoice.getRetentionPercentage() == null
                || invoice.getRetentionPercentage().compareTo(BigDecimal.ZERO) < 0
                || invoice.getRetentionPercentage().compareTo(
                        new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "Retention percentage must be between 0 and 100");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        String currency = invoice.getCurrency();

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        for (InvoiceLine line : lines) {

            if (line.getQuantity() == null
                    || line.getUnitPrice() == null
                    || line.getDiscount() == null) {
                throw new IllegalArgumentException(
                        "Quantity, unit price and discount are required");
            }

            if (line.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                    || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0
                    || line.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Invalid quantity, unit price or discount");
            }

            if (!currency.equalsIgnoreCase(line.getCurrency())) {
                throw new IllegalArgumentException(
                        "All invoice lines must use the invoice currency");
            }

            BigDecimal lineSubtotal =
                    line.getQuantity().multiply(line.getUnitPrice());

            if (line.getDiscount().compareTo(lineSubtotal) > 0) {
                throw new IllegalArgumentException(
                        "Discount cannot exceed line subtotal");
            }

            BigDecimal lineTotal = calculator.calculateLineTotal(
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getDiscount()
            );

            // Ignore any line total sent by the client.
            line.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal retainedAmount = subtotal
                .multiply(invoice.getRetentionPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal amountDue = subtotal.subtract(retainedAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // Ignore client-sent invoice totals.
        invoice.setSubtotal(subtotal);
        invoice.setRetainedAmount(retainedAmount);
        invoice.setTotalAmount(amountDue);
        invoice.setStatus("DRAFT");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (InvoiceLine line : lines) {
            line.setInvoice(savedInvoice);
            invoiceLineRepository.save(line);
        }

        return savedInvoice;
    }
}
