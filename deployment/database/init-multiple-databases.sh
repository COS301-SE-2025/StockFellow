#!/bin/bash
# deployment/database/init-multiple-databases.sh
# Creates multiple databases in a single PostgreSQL instance

set -e
set -u

function create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3
    
    echo "Creating user '$user' and database '$database'..."
    
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        -- Create user if it doesn't exist
        DO \$\$
        BEGIN
            IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$user') THEN
                CREATE USER $user WITH PASSWORD '$password';
            END IF;
        END
        \$\$;
        
        -- Create database if it doesn't exist
        SELECT 'CREATE DATABASE $database OWNER $user'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
        
        -- Grant privileges
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
        
        -- Connect to the new database and grant schema privileges
        \c $database
        GRANT ALL ON SCHEMA public TO $user;
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $user;
        GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $user;
        
        -- Grant default privileges for future objects
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $user;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $user;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO $user;
        
        -- Go back to default database
        \c $POSTGRES_DB
EOSQL
}

function create_database_only() {
    local database=$1
    
    echo "Creating database '$database'..."
    
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $database'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    
    IFS=',' read -ra DATABASES <<< "$POSTGRES_MULTIPLE_DATABASES"
    
    for db in "${DATABASES[@]}"; do
        db=$(echo "$db" | xargs)
        
        case $db in
            "keycloak_db")
                create_database_only "$db"
                ;;
            "userservice_db")
                create_user_and_database "$db" "userservice_user" "userservice_pass"
                ;;
            "notification_db")
                create_user_and_database "$db" "notification_user" "notification_pass"
                ;;
            "admin_db")
                create_user_and_database "$db" "admin_user" "admin_pass"
                ;;
            *)
                create_database_only "$db"
                ;;
        esac
        
        echo "Database '$db' setup completed."
    done
    
    echo "All databases created successfully!"
    
    # List all databases for verification
    echo "Available databases:"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "\l"
    
else
    echo "POSTGRES_MULTIPLE_DATABASES is empty, skipping multiple database creation."
fi