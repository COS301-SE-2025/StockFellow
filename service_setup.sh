#!/bin/bash

# Minimal Services Setup for ID Verification Testing
# This script provides commands to run only the necessary services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO") echo -e "${BLUE}[INFO]${NC} $message" ;;
        "SUCCESS") echo -e "${GREEN}[SUCCESS]${NC} $message" ;;
        "FAILURE") echo -e "${RED}[FAILURE]${NC} $message" ;;
        "WARNING") echo -e "${YELLOW}[WARNING]${NC} $message" ;;
    esac
}

# Function to create docker-compose override file for minimal setup
create_minimal_compose() {
    cat > docker-compose.test.yml << 'EOF'
version: '3.8'

# Minimal services needed for ID verification testing
services:
  # PostgreSQL Database for Keycloak
  postgres:
    image: postgres:15
    container_name: keycloak-postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-keycloak}
      POSTGRES_USER: ${POSTGRES_USER:-keycloak}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-keycloak}
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    networks:
      - stockfellow-net
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "${POSTGRES_USER:-keycloak}"]
      interval: 5s
      timeout: 5s
      retries: 5

  # PostgreSQL Database for Alfresco (needed for document storage)
  alfresco-postgres:
    image: postgres:15
    container_name: alfresco-postgres
    volumes:
      - alfresco_postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: alfresco
      POSTGRES_USER: alfresco
      POSTGRES_PASSWORD: alfresco123
    ports:
      - "5435:5432"
    networks:
      - stockfellow-net
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "alfresco"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis for caching (used by API Gateway)
  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "${REDIS_PORT:-6379}:6379"
    networks:
      - stockfellow-net
    volumes:
      - redis_data:/data
    environment:
      - REDIS_PASSWORD=${REDIS_PASSWORD:-}
    command: redis-server ${REDIS_PASSWORD:+--requirepass $REDIS_PASSWORD}
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  # Alfresco Search Services (required for Alfresco)
  alfresco-search:
    image: alfresco/alfresco-search-services:2.0.8.1
    container_name: alfresco-search
    environment:
      SOLR_ALFRESCO_HOST: alfresco-content-repository
      SOLR_ALFRESCO_PORT: 8080
      SOLR_SOLR_HOST: alfresco-search
      SOLR_SOLR_PORT: 8983
      SOLR_CREATE_ALFRESCO_DEFAULTS: alfresco,archive
      ALFRESCO_SECURE_COMMS: secret
      JAVA_TOOL_OPTIONS: "-Dalfresco.secureComms.secret=secret"
    ports:
      - "8983:8983"
    networks:
      - stockfellow-net
    volumes:
      - alfresco_search_data:/opt/alfresco-search-services/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8983/solr/admin/cores"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # Alfresco Content Repository (for document storage)
  alfresco-content-repository:
    image: alfresco/alfresco-content-repository-community:23.2.1
    container_name: alfresco-content-repository
    depends_on:
      - alfresco-postgres
      - alfresco-search
    environment:
      JAVA_TOOL_OPTIONS: >-
        -Dencryption.keystore.type=JCEKS
        -Dencryption.cipherAlgorithm=DESede/CBC/PKCS5Padding
        -Dencryption.keyAlgorithm=DESede
        -Dencryption.keystore.location=/usr/local/tomcat/shared/classes/alfresco/extension/keystore/keystore
        -Dmetadata-keystore.password=mp6yc0UD9e
        -Dmetadata-keystore.aliases=metadata
        -Dmetadata-keystore.metadata.password=oKIWzVdEdA
        -Dmetadata-keystore.metadata.algorithm=DESede
      JAVA_OPTS: >-
        -Ddb.driver=org.postgresql.Driver
        -Ddb.username=alfresco
        -Ddb.password=alfresco123
        -Ddb.url=jdbc:postgresql://alfresco-postgres:5432/alfresco
        -Dsolr.host=alfresco-search
        -Dsolr.port=8983
        -Dsolr.secureComms=secret
        -Dsolr.sharedSecret=secret
        -Dcsrf.filter.enabled=false
        -Dalfresco.restApi.basicAuthScheme=true
        -Dauthentication.protection.enabled=false
        -XX:MinRAMPercentage=50
        -XX:MaxRAMPercentage=80
    ports:
      - "8080:8080"
    networks:
      - stockfellow-net
    volumes:
      - alfresco_acs_data:/usr/local/tomcat/alf_data
      - ./alfresco/keystore:/usr/local/tomcat/shared/classes/alfresco/extension/keystore
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/probes/-ready-"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 120s

  # Keycloak for authentication
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: keycloak
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-keycloak}
      KC_DB_USERNAME: ${POSTGRES_USER:-keycloak}
      KC_DB_PASSWORD: ${POSTGRES_PASSWORD:-keycloak}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN:-admin}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD:-admin}
      KC_LOG_LEVEL: INFO
    ports:
      - "${KEYCLOAK_PORT:-8180}:8080"
    networks:
      - stockfellow-net
    volumes:
      - ./services/api-gateway/realm-exports:/opt/keycloak/data/import
    command: start-dev --import-realm

  # User Service (the main service we're testing)
  user-service:
    build:
      context: ./services/user-service
      dockerfile: Dockerfile.dev
    container_name: user-service
    depends_on:
      - keycloak
      - alfresco-content-repository
    ports:
      - "${USER_SERVICE_PORT:-4000}:4000"
      - "${USER_SERVICE_DEBUG_PORT:-5005}:5005"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - SPRING_DATA_MONGODB_URI=${USER_SERVICE_MONGODB_URI:-mongodb://localhost:27017/userservice_test}
      - KEYCLOAK_JWKS_URI=http://keycloak:8080/realms/stockfellow/protocol/openid-connect/certs
      - KEYCLOAK_ISSUER=http://keycloak:8080/realms/stockfellow
      - ALFRESCO_BASE_URL=http://alfresco-content-repository:8080/alfresco
      - ALFRESCO_USERNAME=admin
      - ALFRESCO_PASSWORD=admin
      - ALFRESCO_SITE_NAME=stockfellow
      - JVM_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
      - LOGGING_LEVEL_ROOT=INFO
      - LOGGING_LEVEL_COM_STOCKFELLOW=DEBUG
    networks:
      - stockfellow-net
    volumes:
      - ./services/user-service/src:/app/src
      - ./services/user-service/pom.xml:/app/pom.xml
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:4000/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

networks:
  stockfellow-net:
    driver: bridge

volumes:
  postgres_data:
  alfresco_postgres_data:
  redis_data:
  alfresco_acs_data:
  alfresco_search_data:
EOF

    print_status "SUCCESS" "Created docker-compose.test.yml with minimal services"
}

# Function to create test environment file
create_test_env() {
    cat > .env.test << 'EOF'
# Test Environment Configuration

# Database settings
POSTGRES_DB=keycloak
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=keycloak
POSTGRES_PORT=5432

# Redis settings
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Keycloak settings
KEYCLOAK_PORT=8180
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# User Service settings
USER_SERVICE_PORT=4000
USER_SERVICE_DEBUG_PORT=5005
USER_SERVICE_MONGODB_URI=mongodb://localhost:27017/userservice_test

# Health check settings
HEALTH_CHECK_INTERVAL=30s
HEALTH_CHECK_TIMEOUT=10s
HEALTH_CHECK_RETRIES=3
HEALTH_CHECK_START_PERIOD=60s

# Logging
ROOT_LOG_LEVEL=INFO
LOG_LEVEL=DEBUG
EOF

    print_status "SUCCESS" "Created .env.test file"
}

# Function to start minimal services
start_minimal_services() {
    print_status "INFO" "Starting minimal services for ID verification testing..."
    
    # Create necessary files if they don't exist
    if [ ! -f "docker-compose.test.yml" ]; then
        create_minimal_compose
    fi
    
    if [ ! -f ".env.test" ]; then
        create_test_env
    fi
    
    # Start services
    docker-compose -f docker-compose.test.yml --env-file .env.test up -d
    
    print_status "SUCCESS" "Services started. Use 'docker-compose -f docker-compose.test.yml logs -f' to view logs"
}

# Function to stop services
stop_services() {
    print_status "INFO" "Stopping services..."
    docker-compose -f docker-compose.test.yml down
    print_status "SUCCESS" "Services stopped"
}

# Function to clean up everything
clean_all() {
    print_status "INFO" "Cleaning up all test resources..."
    docker-compose -f docker-compose.test.yml down -v
    docker system prune -f
    rm -f docker-compose.test.yml .env.test
    print_status "SUCCESS" "Cleanup completed"
}

# Function to show service status
show_status() {
    print_status "INFO" "Service Status:"
    echo ""
    docker-compose -f docker-compose.test.yml ps
}

# Function to show logs
show_logs() {
    local service=${1:-}
    if [ -n "$service" ]; then
        docker-compose -f docker-compose.test.yml logs -f "$service"
    else
        docker-compose -f docker-compose.test.yml logs -f
    fi
}

# Function to run health checks
health_check() {
    print_status "INFO" "Checking service health..."
    
    local services=(
        "postgres:5432"
        "alfresco-postgres:5435"  
        "redis:6379"
        "keycloak:8180"
        "alfresco-content-repository:8080"
        "user-service:4000"
    )
    
    for service in "${services[@]}"; do
        local name=$(echo "$service" | cut -d: -f1)
        local port=$(echo "$service" | cut -d: -f2)
        
        if nc -z localhost "$port" 2>/dev/null; then
            print_status "SUCCESS" "$name is healthy (port $port)"
        else
            print_status "FAILURE" "$name is not responding (port $port)"
        fi
    done
}

# Main execution
case "${1:-start}" in
    "start"|"up")
        start_minimal_services
        ;;
    "stop"|"down") 
        stop_services
        ;;
    "clean"|"cleanup")
        clean_all
        ;;
    "status"|"ps")
        show_status
        ;;
    "logs")
        show_logs "$2"
        ;;
    "health"|"check")
        health_check
        ;;
    "create-files")
        create_minimal_compose
        create_test_env
        ;;
    *)
        echo "Usage: $0 {start|stop|clean|status|logs|health|create-files}"
        echo ""
        echo "Commands:"
        echo "  start       - Start minimal services for testing"
        echo "  stop        - Stop all services"
        echo "  clean       - Stop services and remove volumes"
        echo "  status      - Show service status"
        echo "  logs [svc]  - Show logs (optionally for specific service)"
        echo "  health      - Check service health"
        echo "  create-files- Create docker-compose.test.yml and .env.test"
        exit 1
        ;;
esac