package com.example.paytrack.controller;

import com.example.paytrack.entity.AppUser;
import com.example.paytrack.entity.milestone.JobMilestone;
import com.example.paytrack.repository.AppUserRepository;
import com.example.paytrack.service.MilestoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final AppUserRepository appUserRepository;

    public MilestoneController(
            MilestoneService milestoneService,
            AppUserRepository appUserRepository) {
        this.milestoneService = milestoneService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    public ResponseEntity<?> createMilestone(
            @RequestBody CreateMilestoneRequest request,
            Authentication authentication) {

        AppUser user = appUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobMilestone milestone = new JobMilestone();
        milestone.setJobName(request.jobName());
        milestone.setDescription(request.description());

        JobMilestone saved = milestoneService.createMilestone(
                milestone, user.getOrganisation());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", saved.getId(),
                        "jobName", saved.getJobName(),
                        "description", saved.getDescription(),
                        "status", saved.getStatus()
                )
        );
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeMilestone(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser user = appUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobMilestone completed = milestoneService.completeMilestone(
                id, user.getOrganisation().getId());

        return ResponseEntity.ok(
                Map.of(
                        "id", completed.getId(),
                        "jobName", completed.getJobName(),
                        "status", completed.getStatus(),
                        "completedAt", completed.getCompletedAt()
                )
        );
    }

    public record CreateMilestoneRequest(
            String jobName,
            String description
    ) {
    }
}