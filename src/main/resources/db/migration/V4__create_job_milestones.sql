
CREATE TABLE job_milestones (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(150) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP,
    organisation_id BIGINT NOT NULL,

    CONSTRAINT fk_milestone_organisation
        FOREIGN KEY (organisation_id)
        REFERENCES organisations(id)
);

ALTER TABLE invoices
ADD COLUMN milestone_id BIGINT;

ALTER TABLE invoices
ADD CONSTRAINT fk_invoice_milestone
FOREIGN KEY (milestone_id)
REFERENCES job_milestones(id);
