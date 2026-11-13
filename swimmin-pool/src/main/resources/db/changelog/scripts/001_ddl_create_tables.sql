CREATE TABLE IF NOT EXISTS report(
    id SERIAL PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    file_path VARCHAR(500),
    generated_at TIMESTAMP,
    generated_by VARCHAR(20) CHECK(generated_by IN ('SYSTEM', 'MANUAL'))
);

CREATE TABLE IF NOT EXISTS client(
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    birth_date DATE NOT NULL,
    UNIQUE(first_name, last_name, surname, birth_date)
);

CREATE TABLE IF NOT EXISTS subscription(
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    subscription_type VARCHAR(30) NOT NULL CHECK(subscription_type IN ('ONCE_ONLY', 'VISIT4', 'VISIT8', 'VISIT12', 'NO_LIMIT')),
    status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE', 'EXPIRED', 'EXHAUSTED')) DEFAULT 'ACTIVE',
    expire DATE NOT NULL,
    client_id BIGINT REFERENCES client(id) NOT NULL,
    created_at DATE NOT NULL CHECK(created_at < expire)
);

CREATE TABLE IF NOT EXISTS visit(
    id SERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES client(id) NOT NULL,
    subscription_id VARCHAR(255) REFERENCES subscription(id) NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);