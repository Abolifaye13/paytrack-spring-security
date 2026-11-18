
package com.example.paytrack.controller;

import com.example.paytrack.entity.AppUser;
import com.example.paytrack.entity.OrganisationRecord;
import com.example.paytrack.repository.AppUserRepository;
import com.example.paytrack.repository.OrganisationRecordRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final AppUserRepository appUserRepository;
    private final OrganisationRecordRepository recordRepository;

    public RecordController(
            AppUserRepository appUserRepository,
            OrganisationRecordRepository recordRepository) {
        this.appUserRepository = appUserRepository;
        this.recordRepository = recordRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getRecords(
            Authentication authentication) {

        AppUser user = appUserRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long organisationId = user.getOrganisation().getId();

        List<OrganisationRecord> records =
                recordRepository.findByOrganisation_Id(organisationId);

        return records.stream()
                .map(record -> Map.<String, Object>of(
                        "id", record.getId(),
                        "description", record.getDescription(),
                        "amount", record.getAmount()
                ))
                .toList();
    }
}
