CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    financial_tier VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== mandates ==========
CREATE TABLE IF NOT EXISTS mandates (
  mandate_id UUID PRIMARY KEY,
  payer_user_id UUID NOT NULL,
  group_id UUID NOT NULL,
  payment_method_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  signed_date TIMESTAMP NOT NULL,
  document_reference VARCHAR(255),
  ip_address INET NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS mandates_payer_group_idx ON mandates (payer_user_id, group_id);
CREATE INDEX IF NOT EXISTS mandates_status_group_idx ON mandates (status, group_id);

-- ========== group_cycles ==========
CREATE TABLE IF NOT EXISTS group_cycles (
  cycle_id UUID PRIMARY KEY,
  group_id UUID NOT NULL,
  cycle_month VARCHAR(7) NOT NULL,
  recipient_user_id UUID NOT NULL,
  recipient_payment_method_id UUID NOT NULL,
  contribution_amount DECIMAL(10,2) NOT NULL,
  collection_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  total_expected_amount DECIMAL(12,2) NOT NULL,
  total_collected_amount DECIMAL(12,2) DEFAULT 0,
  successful_payments INTEGER DEFAULT 0,
  failed_payments INTEGER DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS cycles_group_month_idx ON group_cycles (group_id, cycle_month);
CREATE INDEX IF NOT EXISTS cycles_status_date_idx ON group_cycles (status, collection_date);
CREATE INDEX IF NOT EXISTS cycles_recipient_month_idx ON group_cycles (recipient_user_id, cycle_month);

-- ========== transactions ==========
CREATE TABLE IF NOT EXISTS transactions (
  transaction_id UUID PRIMARY KEY,
  cycle_id UUID NOT NULL,
  mandate_id UUID NOT NULL,
  payer_user_id UUID NOT NULL,
  recipient_user_id UUID NOT NULL,
  group_id UUID NOT NULL,
  payer_payment_method_id UUID NOT NULL,
  recipient_payment_method_id UUID NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  external_reference VARCHAR(255),
  retry_count INTEGER DEFAULT 0,
  fail_message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS transactions_cycle_status_idx ON transactions (cycle_id, status);
CREATE INDEX IF NOT EXISTS transactions_payer_date_idx ON transactions (payer_user_id, created_at);
CREATE INDEX IF NOT EXISTS transactions_recipient_date_idx ON transactions (recipient_user_id, created_at);
CREATE INDEX IF NOT EXISTS transactions_status_date_idx ON transactions (status, created_at);

-- ========== payment_attempts ==========
CREATE TABLE IF NOT EXISTS payment_attempts (
  attempt_id UUID PRIMARY KEY,
  transaction_id UUID NOT NULL,
  attempt_number INTEGER NOT NULL,
  attempted_at TIMESTAMP NOT NULL DEFAULT NOW(),
  response_code VARCHAR(10),
  response_message TEXT,
  processing_fee DECIMAL(10,2) DEFAULT 0,
  status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS attempts_txn_number_idx ON payment_attempts (transaction_id, attempt_number);

-- ========== member_balances ==========
CREATE TABLE IF NOT EXISTS member_balances (
  balance_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  group_id UUID NOT NULL,
  total_contributed DECIMAL(12,2) DEFAULT 0,
  total_received DECIMAL(12,2) DEFAULT 0,
  last_contribution_date DATE,
  last_receipt_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS balances_user_group_idx ON member_balances (user_id, group_id);
CREATE INDEX IF NOT EXISTS balances_group_idx ON member_balances (group_id);

-- ========== transaction_events ==========
CREATE TABLE IF NOT EXISTS transaction_events (
  event_id UUID PRIMARY KEY,
  transaction_id UUID NOT NULL,
  event_type VARCHAR(30) NOT NULL,
  previous_status VARCHAR(20),
  new_status VARCHAR(20) NOT NULL,
  triggered_by VARCHAR(50),
  metadata JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS events_txn_created_idx ON transaction_events (transaction_id, created_at);
CREATE INDEX IF NOT EXISTS events_type_created_idx ON transaction_events (event_type, created_at);

-- ========== payment_method_changes ==========
CREATE TABLE IF NOT EXISTS payment_method_changes (
    change_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(36) NOT NULL, // Changed from UUID to VARCHAR(36)
    old_method_id UUID,
    new_method_id UUID NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    flagged BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address INET,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS payment_changes_user_id_idx ON payment_method_changes(user_id);
CREATE INDEX IF NOT EXISTS payment_changes_timestamp_idx ON payment_method_changes(timestamp);
CREATE INDEX IF NOT EXISTS payment_changes_flagged_idx ON payment_method_changes(flagged);

-- ========== suspicious_users ==========
CREATE TABLE IF NOT EXISTS suspicious_users (
    suspicious_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(36) NOT NULL,  -- Changed from UUID to VARCHAR(36)
    reason VARCHAR(500) NOT NULL,
    flagged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(255),
    severity VARCHAR(20) DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS suspicious_users_user_id_idx ON suspicious_users(user_id);
CREATE INDEX IF NOT EXISTS suspicious_users_reviewed_idx ON suspicious_users(reviewed);
CREATE INDEX IF NOT EXISTS suspicious_users_flagged_at_idx ON suspicious_users(flagged_at);
CREATE INDEX IF NOT EXISTS suspicious_users_severity_idx ON suspicious_users(severity);

-- ========== Foreign Keys ==========
ALTER TABLE transactions
ADD CONSTRAINT IF NOT EXISTS fk_transactions_cycle 
  FOREIGN KEY (cycle_id) REFERENCES group_cycles(cycle_id);

ALTER TABLE transactions
  ADD CONSTRAINT IF NOT EXISTS fk_transactions_mandate 
  FOREIGN KEY (mandate_id) REFERENCES mandates(mandate_id);

ALTER TABLE payment_attempts
  ADD CONSTRAINT IF NOT EXISTS fk_attempts_transaction 
  FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id);

ALTER TABLE transaction_events
  ADD CONSTRAINT IF NOT EXISTS fk_events_transaction 
  FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id);

--  foreign key constraints for fraud detection tables  ///////// just added
ALTER TABLE payment_method_changes
  ADD CONSTRAINT IF NOT EXISTS fk_payment_changes_user 
  FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE suspicious_users
  ADD CONSTRAINT IF NOT EXISTS fk_suspicious_users_user 
  FOREIGN KEY (user_id) REFERENCES users(user_id);