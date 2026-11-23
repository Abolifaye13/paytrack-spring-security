
package com.example.paytrack.service;

import com.example.paytrack.entity.Organisation;
import com.example.paytrack.entity.invoice.Invoice;
import com.example.paytrack.entity.milestone.JobMilestone;
import com.example.paytrack.repository.InvoiceRepository;
import com.example.paytrack.repository.JobMilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilestoneService {

    private final JobMilestoneRepository milestoneRepository;
    private final InvoiceRepository invoiceRepository;

    public MilestoneService(
            JobMilestoneRepository milestoneRepository,
            InvoiceRepository invoiceRepository) {
        this.milestoneRepository = milestoneRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public JobMilestone createMilestone(
            JobMilestone milestone, Organisation organisation) {

        if (milestone.getJobName() == null
                || milestone.getJobName().isBlank()) {
            throw new IllegalArgumentException("Job name is required");
        }

        if (milestone.getDescription() == null
                || milestone.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        milestone.setOrganisation(organisation);
        milestone.setStatus("PENDING");
        milestone.setCompletedAt(null);

        return milestoneRepository.save(milestone);
    }

    @Transactional
    public JobMilestone completeMilestone(
            Long milestoneId, Long organisationId) {

        JobMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Milestone not found"));

        if (!milestone.getOrganisation().getId().equals(organisationId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to complete this milestone");
        }

        if (!"COMPLETED".equals(milestone.getStatus())) {

            milestone.setStatus("COMPLETED");
            milestone.setCompletedAt(LocalDateTime.now());

            milestoneRepository.save(milestone);

            List<Invoice> invoices =
                    invoiceRepository.findByMilestone_Id(milestoneId);

            for (Invoice invoice : invoices) {

                BigDecimal remainingHeld =
                        invoice.getRemainingHeldAmount();

                if (remainingHeld.compareTo(BigDecimal.ZERO) > 0) {

                    invoice.setReleasedAmount(
                            invoice.getReleasedAmount().add(remainingHeld));

                    invoiceRepository.save(invoice);
                }
            }
        }

        return milestone;
    }
}
