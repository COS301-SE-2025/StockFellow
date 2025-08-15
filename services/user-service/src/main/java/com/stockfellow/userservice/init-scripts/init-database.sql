-- Database initialization script for User Service
-- This file should be placed in ./services/user-service/init-scripts/

-- Create database and user (if not done by Docker)
-- Note: These are usually handled by the Docker environment variables
-- but included here for completeness

-- Ensure UTF-8 encoding
SET client_encoding = 'UTF8';

-- Create the users table
-- (Hibernate will also create this, but having it here ensures consistency)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email_verified BOOLEAN DEFAULT FALSE,
    contact_number VARCHAR(20),
    id_number VARCHAR(13),
    id_verified BOOLEAN DEFAULT FALSE,
    alfresco_document_id VARCHAR(255),
    date_of_birth VARCHAR(10),
    gender VARCHAR(10),
    citizenship VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_id_number ON users(id_number) WHERE id_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_updated_at ON users(updated_at);
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);
CREATE INDEX IF NOT EXISTS idx_users_id_verified ON users(id_verified);
CREATE INDEX IF NOT EXISTS idx_users_name ON users(first_name, last_name);

-- Create a composite index for common search patterns
CREATE INDEX IF NOT EXISTS idx_users_verification_status ON users(id_verified, email_verified);

-- Add constraints
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_email_format 
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_id_number_length 
    CHECK (id_number IS NULL OR length(id_number) = 13);

ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_contact_number_format 
    CHECK (contact_number IS NULL OR contact_number ~* '^\+?[0-9\s\-\(\)]+$');

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some test data (optional - remove in production)
-- This is useful for development testing
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 'test-user-1') THEN
        INSERT INTO users (
            user_id, username, email, first_name, last_name, 
            email_verified, contact_number, created_at, updated_at
        ) VALUES (
            'test-user-1', 
            'testuser', 
            'test@example.com', 
            'Test', 
            'User', 
            true, 
            '+27123456789', 
            CURRENT_TIMESTAMP, 
            CURRENT_TIMESTAMP
        );
    END IF;
END $$;

-- Create a view for user statistics (useful for admin dashboards)
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN email_verified = true THEN 1 END) as email_verified_users,
    COUNT(CASE WHEN id_verified = true THEN 1 END) as id_verified_users,
    COUNT(CASE WHEN id_verified = true AND email_verified = true THEN 1 END) as fully_verified_users,
    COUNT(CASE WHEN first_name IS NULL OR first_name = '' OR 
                   last_name IS NULL OR last_name = '' OR 
                   contact_number IS NULL OR contact_number = '' THEN 1 END) as incomplete_profiles,
    COUNT(CASE WHEN created_at >= CURRENT_DATE THEN 1 END) as users_created_today,
    COUNT(CASE WHEN created_at >= CURRENT_DATE - INTERVAL '7 days' THEN 1 END) as users_created_week,
    COUNT(CASE WHEN created_at >= CURRENT_DATE - INTERVAL '30 days' THEN 1 END) as users_created_month
FROM users;

-- Create a view for user details (excludes sensitive information)
CREATE OR REPLACE VIEW user_summary AS
SELECT 
    user_id,
    username,
    email,
    first_name,
    last_name,
    email_verified,
    id_verified,
    CASE 
        WHEN first_name IS NOT NULL AND first_name != '' AND
             last_name IS NOT NULL AND last_name != '' AND
             contact_number IS NOT NULL AND contact_number != '' 
        THEN true 
        ELSE false 
    END as profile_complete,
    created_at,
    updated_at
FROM users;

-- Grant permissions (adjust as needed for your setup)
GRANT SELECT, INSERT, UPDATE, DELETE ON users TO userservice_user;
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO userservice_user;
GRANT SELECT ON user_statistics TO userservice_user;
GRANT SELECT ON user_summary TO userservice_user;

-- Add comments for documentation
COMMENT ON TABLE users IS 'Main users table storing user information from Keycloak and ID verification data';
COMMENT ON COLUMN users.user_id IS 'Keycloak user ID - primary identifier';
COMMENT ON COLUMN users.id_number IS 'South African ID number - 13 digits';
COMMENT ON COLUMN users.alfresco_document_id IS 'Reference to ID document stored in Alfresco';
COMMENT ON COLUMN users.date_of_birth IS 'Date of birth extracted from SA ID (YYYY-MM-DD format)';
COMMENT ON COLUMN users.citizenship IS 'Citizenship status extracted from SA ID';

COMMIT;