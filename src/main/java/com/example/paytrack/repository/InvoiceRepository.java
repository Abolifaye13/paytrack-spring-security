
package com.example.paytrack.repository;

import com.example.paytrack.entity.invoice.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByOrganisation_Id(Long organisationId);

    List<Invoice> findByMilestone_Id(Long milestoneId);
}
