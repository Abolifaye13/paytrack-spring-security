
package com.example.paytrack.service;

import com.example.paytrack.entity.Organisation;
import com.example.paytrack.entity.invoice.Invoice;
import com.example.paytrack.entity.milestone.JobMilestone;
import com.example.paytrack.repository.InvoiceRepository;
import com.example.paytrack.repository.JobMilestoneRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    @Mock
    private JobMilestoneRepository milestoneRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private MilestoneService milestoneService;

    @Test
    void completingMilestoneReleasesRemainingRetention() {

        Long milestoneId = 1L;
        Long organisationId = 10L;

        Organisation organisation = mock(Organisation.class);
        when(organisation.getId()).thenReturn(organisationId);

        JobMilestone milestone = new JobMilestone();
        milestone.setOrganisation(organisation);
        milestone.setStatus("PENDING");

        Invoice invoice = new Invoice();
        invoice.setRetainedAmount(new BigDecimal("1000.00"));
        invoice.setReleasedAmount(new BigDecimal("200.00"));

        when(milestoneRepository.findById(milestoneId))
                .thenReturn(Optional.of(milestone));

        when(invoiceRepository.findByMilestone_Id(milestoneId))
                .thenReturn(List.of(invoice));

        when(milestoneRepository.save(milestone))
                .thenReturn(milestone);

        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);

        JobMilestone result =
                milestoneService.completeMilestone(
                        milestoneId, organisationId);

        assertEquals("COMPLETED", result.getStatus());

        assertEquals(
                new BigDecimal("1000.00"),
                invoice.getReleasedAmount());

        assertEquals(
                new BigDecimal("0.00"),
                invoice.getRemainingHeldAmount());

        verify(milestoneRepository).save(milestone);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void completingAlreadyCompletedMilestoneDoesNotReleaseAgain() {

        Long milestoneId = 1L;
        Long organisationId = 10L;

        Organisation organisation = mock(Organisation.class);
        when(organisation.getId()).thenReturn(organisationId);

        JobMilestone milestone = new JobMilestone();
        milestone.setOrganisation(organisation);
        milestone.setStatus("COMPLETED");

        when(milestoneRepository.findById(milestoneId))
                .thenReturn(Optional.of(milestone));

        JobMilestone result =
                milestoneService.completeMilestone(
                        milestoneId, organisationId);

        assertEquals("COMPLETED", result.getStatus());

        verify(milestoneRepository, never()).save(any());
        verify(invoiceRepository, never())
                .findByMilestone_Id(anyLong());
        verify(invoiceRepository, never()).save(any());
    }
}
