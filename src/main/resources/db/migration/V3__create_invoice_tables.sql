
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    subtotal DECIMAL(12,2) NOT NULL,
    retention_percentage DECIMAL(5,2) NOT NULL,
    retained_amount DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    organisation_id BIGINT NOT NULL,

    CONSTRAINT fk_invoice_organisation
        FOREIGN KEY (organisation_id)
        REFERENCES organisations(id),

    CONSTRAINT chk_retention_percentage
        CHECK (retention_percentage >= 0 AND retention_percentage <= 100)
);

CREATE TABLE invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    quantity DECIMAL(12,3) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    invoice_id BIGINT NOT NULL,

    CONSTRAINT fk_invoice_line
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE CASCADE
);

