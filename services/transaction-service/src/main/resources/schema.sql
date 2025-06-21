CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    financial_tier VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mandates (
    mandate_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(user_id),
    bank_account VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(user_id),
    group_id VARCHAR(36),
    type VARCHAR(20) NOT NULL, -- DEBIT_ORDER, PAYOUT
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    external_ref VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedules (
    schedule_id VARCHAR(36) PRIMARY KEY,
    group_id VARCHAR(36),
    user_id VARCHAR(36) REFERENCES users(user_id),
    type VARCHAR(20) NOT NULL, -- DEBIT_ORDER, PAYOUT
    amount DECIMAL(10, 2) NOT NULL,
    frequency VARCHAR(20) NOT NULL, -- MONTHLY, BI_WEEKLY, WEEKLY
    next_run DATE NOT NULL,
    status VARCHAR(20) NOT NULL -- ACTIVE, INACTIVE
);