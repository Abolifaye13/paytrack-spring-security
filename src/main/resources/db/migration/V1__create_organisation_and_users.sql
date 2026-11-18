
CREATE TABLE organisations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    organisation_id BIGINT NOT NULL,
    CONSTRAINT fk_user_organisation
        FOREIGN KEY (organisation_id)
        REFERENCES organisations(id)
);

CREATE TABLE organisation_records (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    organisation_id BIGINT NOT NULL,
    CONSTRAINT fk_record_organisation
        FOREIGN KEY (organisation_id)
        REFERENCES organisations(id)
);

INSERT INTO organisations (name)
VALUES
    ('Organisation A'),
    ('Organisation B');

-- Development login password for both users: password
-- BCrypt hash, not a plain-text database password.

INSERT INTO app_users
(name, email, password, role, organisation_id)
VALUES
(
    'Admin A',
    'adminA@paytrack.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    1
),
(
    'Admin B',
    'adminB@paytrack.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    2
);

INSERT INTO organisation_records
(description, amount, organisation_id)
VALUES
    ('Organisation A - Office Expense', 5000.00, 1),
    ('Organisation B - Travel Expense', 3000.00, 2);
