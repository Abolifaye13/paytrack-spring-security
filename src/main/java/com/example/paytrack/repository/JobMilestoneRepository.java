
package com.example.paytrack.repository;

import com.example.paytrack.entity.milestone.JobMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobMilestoneRepository
        extends JpaRepository<JobMilestone, Long> {

    List<JobMilestone> findByOrganisation_Id(Long organisationId);

}
