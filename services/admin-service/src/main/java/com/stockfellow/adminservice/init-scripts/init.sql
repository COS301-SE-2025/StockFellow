-- Admin Service Database Schema

-- Enable JSON extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Daily metrics table for analytics
CREATE TABLE IF NOT EXISTS daily_metrics (
    date DATE PRIMARY KEY,
    new_users BIGINT DEFAULT 0,
    active_users BIGINT DEFAULT 0,
    new_groups BIGINT DEFAULT 0,
    total_transactions BIGINT DEFAULT 0,
    transaction_volume DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User growth tracking
CREATE TABLE IF NOT EXISTS user_growth_metrics (
    date DATE PRIMARY KEY,
    cumulative_users BIGINT,
    verified_users BIGINT,
    tier_distribution JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255),
    endpoint VARCHAR(500) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_payload TEXT,
    response_status VARCHAR(10),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(255),
    headers JSONB,
    risk_score INTEGER DEFAULT 0,
    risk_factors TEXT,
    geolocation VARCHAR(100),
    flagged_for_review BOOLEAN DEFAULT FALSE
);

-- Create indexes for audit logs
CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_logs (user_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_endpoint ON audit_logs (endpoint);
CREATE INDEX IF NOT EXISTS idx_audit_flagged ON audit_logs (flagged_for_review);
CREATE INDEX IF NOT EXISTS idx_audit_risk_score ON audit_logs (risk_score);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs (timestamp);

-- Admin requests table
CREATE TABLE IF NOT EXISTS admin_requests (
    request_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    group_id VARCHAR(255),
    card_id VARCHAR(255),
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_user_id VARCHAR(255),
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    metadata JSONB
);

-- Create indexes for admin requests
CREATE INDEX IF NOT EXISTS idx_admin_requests_status ON admin_requests (status);
CREATE INDEX IF NOT EXISTS idx_admin_requests_user_type ON admin_requests (user_id, request_type);
CREATE INDEX IF NOT EXISTS idx_admin_requests_created_at ON admin_requests (created_at);
CREATE INDEX IF NOT EXISTS idx_admin_requests_type ON admin_requests (request_type);

-- Insert some initial metrics data
INSERT INTO daily_metrics (date, new_users, active_users, new_groups, total_transactions, transaction_volume)
VALUES 
    (CURRENT_DATE - INTERVAL '7 days', 15, 120, 3, 45, 12500.00),
    (CURRENT_DATE - INTERVAL '6 days', 12, 135, 2, 52, 15200.00),
    (CURRENT_DATE - INTERVAL '5 days', 18, 142, 4, 48, 13800.00),
    (CURRENT_DATE - INTERVAL '4 days', 22, 158, 1, 61, 18900.00),
    (CURRENT_DATE - INTERVAL '3 days', 16, 167, 5, 55, 16700.00),
    (CURRENT_DATE - INTERVAL '2 days', 20, 175, 2, 59, 17200.00),
    (CURRENT_DATE - INTERVAL '1 day', 25, 183, 3, 67, 19800.00),
    (CURRENT_DATE, 0, 190, 0, 12, 3200.00)
ON CONFLICT (date) DO NOTHING;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO admin_user;