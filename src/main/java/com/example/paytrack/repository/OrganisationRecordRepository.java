package com.example.paytrack.repository;

import com.example.paytrack.entity.OrganisationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganisationRecordRepository
        extends JpaRepository<OrganisationRecord, Long> {

    List<OrganisationRecord> findByOrganisation_Id(Long organisationId);
}