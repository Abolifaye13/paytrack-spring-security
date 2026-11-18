
package com.example.paytrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "organisation_records")
public class OrganisationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    public OrganisationRecord() {
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Organisation getOrganisation() {
        return organisation;
    }
}
