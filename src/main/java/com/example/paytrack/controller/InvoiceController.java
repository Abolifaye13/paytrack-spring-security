
package com.example.paytrack.controller;

import com.example.paytrack.entity.AppUser;
import com.example.paytrack.entity.invoice.Invoice;
import com.example.paytrack.entity.invoice.InvoiceLine;
import com.example.paytrack.repository.AppUserRepository;
import com.example.paytrack.repository.InvoiceRepository;
import com.example.paytrack.service.InvoiceService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AppUserRepository appUserRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(
            InvoiceService invoiceService,
            AppUserRepository appUserRepository,
            InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.appUserRepository = appUserRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/draft")
    public ResponseEntity<Invoice> createDraft(
            @RequestBody InvoiceDraftRequest request,
            Authentication authentication) {

        AppUser user = appUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getOrganisation() == null) {
            throw new RuntimeException(
                    "User is not associated with an organisation");
        }

        Invoice invoice = request.getInvoice();
        invoice.setOrganisation(user.getOrganisation());

        Invoice savedInvoice = invoiceService.createDraft(
                invoice, request.getLines());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedInvoice);
    }

    @GetMapping
    public ResponseEntity<?> getInvoices(
            Authentication authentication) {

        AppUser user = appUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getOrganisation() == null) {
            throw new RuntimeException(
                    "User is not associated with an organisation");
        }

        List<Invoice> invoices =
                invoiceRepository.findByOrganisation_Id(
                        user.getOrganisation().getId());

        List<Map<String, Object>> response = invoices.stream()
                .map(invoice -> Map.<String, Object>ofEntries(
                        Map.entry("id", invoice.getId()),
                        Map.entry("invoiceNumber", invoice.getInvoiceNumber()),
                        Map.entry("subtotal", invoice.getSubtotal()),
                        Map.entry("retentionPercentage", invoice.getRetentionPercentage()),
                        Map.entry("retainedAmount", invoice.getRetainedAmount()),
                        Map.entry("releasedAmount", invoice.getReleasedAmount()),
                        Map.entry("remainingHeldAmount", invoice.getRemainingHeldAmount()),
                        Map.entry("totalAmount", invoice.getTotalAmount()),
                        Map.entry("currency", invoice.getCurrency()),
                        Map.entry("status", invoice.getStatus()),
                        Map.entry("milestoneId",
                                invoice.getMilestone() == null
                                        ? "Not linked"
                                        : invoice.getMilestone().getId())
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    public static class InvoiceDraftRequest {

        private Invoice invoice;
        private List<InvoiceLine> lines;

        public Invoice getInvoice() {
            return invoice;
        }

        public void setInvoice(Invoice invoice) {
            this.invoice = invoice;
        }

        public List<InvoiceLine> getLines() {
            return lines;
        }

        public void setLines(List<InvoiceLine> lines) {
            this.lines = lines;
        }
    }
}
