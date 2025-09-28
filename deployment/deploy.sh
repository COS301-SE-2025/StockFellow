#!/bin/bash
# deploy.sh - Environment deployment helper script

set -e

ENVIRONMENT=${1:-"dev"}
ACTION=${2:-"up"}

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

function print_usage() {
    echo "Usage: $0 [environment] [action]"
    echo ""
    echo "Environments:"
    echo "  dev     - Development environment (no resource limits, debug ports)"
    echo "  prod    - Production environment (resource constrained, security hardened)"
    echo ""
    echo "Actions:"
    echo "  up      - Start services"
    echo "  down    - Stop services"
    echo "  build   - Build images"
    echo "  logs    - Show logs"
    echo "  health  - Check service health"
    echo "  clean   - Clean up volumes and images"
    echo "  keycloak-debug - Debug Keycloak realm import"
    echo "  keycloak-logs  - Show Keycloak logs with import filtering"
    echo ""
    echo "Examples:"
    echo "  $0 dev up       # Start development environment"
    echo "  $0 prod build   # Build production images"
    echo "  $0 prod logs    # Show production logs"
}

function log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

function log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

function log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

function log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

function log_debug() {
    echo -e "${PURPLE}[DEBUG]${NC} $1"
}

function check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    # Check if database init script exists
    if [ ! -f "./database/init-multiple-databases.sh" ]; then
        log_warning "Database init script not found. Creating directory..."
        mkdir -p ./database
        log_info "Please ensure the init-multiple-databases.sh script is in ./deployment/database/ directory"
    fi
    
    # Check if .env file exists
    if [ ! -f ".env" ]; then
        log_warning ".env file not found. Creating sample..."
    fi

    check_keycloak_files

    log_success "Prerequisites check completed"
}

function check_keycloak_files() {
    log_info "Checking Keycloak realm files..."
    
    if [ -d "./services/api-gateway/realm-exports" ]; then
        local realm_files=$(find ./services/api-gateway/realm-exports -name "*.json" | wc -l)
        if [ $realm_files -gt 0 ]; then
            log_success "Found $realm_files realm file(s)"
            ls -la ./services/api-gateway/realm-exports/*.json | while read line; do
                log_debug "  $line"
            done
        else
            log_warning "No JSON realm files found in ./services/api-gateway/realm-exports"
        fi
    else
        log_warning "Realm exports directory not found: ./services/api-gateway/realm-exports"
    fi
    
    if [ -d "./integrations/keycloak-extensions" ]; then
        local ext_files=$(find ./integrations/keycloak-extensions -name "*.jar" | wc -l)
        if [ $ext_files -gt 0 ]; then
            log_success "Found $ext_files Keycloak extension(s)"
        else
            log_info "No Keycloak extensions found"
        fi
    fi
}

function ensure_executable() {
    if [ -f "./database/init-multiple-databases.sh" ]; then
        chmod +x ./database/init-multiple-databases.sh
        log_info "Made database init script executable"
    fi
}

function monitor_keycloak_startup() {
    local env=$1
    local compose_file
    

    compose_file="./docker-compose.prod.yml"
    
    log_info "Monitoring Keycloak startup and realm import..."
    
    # Wait for container to start
    sleep 5
    
    local container_name="keycloak"
    
    # Check if container is running
    if ! docker ps | grep -q "$container_name"; then
        log_error "Keycloak container not found or not running"
        return 1
    fi
    
    log_info "Tailing Keycloak logs for import information..."
    echo -e "${YELLOW}Press Ctrl+C to stop monitoring${NC}"
    
    # Monitor logs with filtering for important events
    docker logs -f "$container_name" 2>&1 | while IFS= read -r line; do
        case "$line" in
            *"import"*|*"Import"*|*"IMPORT"*)
                echo -e "${GREEN}[IMPORT]${NC} $line"
                ;;
            *"realm"*|*"Realm"*|*"REALM"*)
                echo -e "${BLUE}[REALM]${NC} $line"
                ;;
            *"ERROR"*|*"error"*)
                echo -e "${RED}[ERROR]${NC} $line"
                ;;
            *"WARN"*|*"warn"*)
                echo -e "${YELLOW}[WARN]${NC} $line"
                ;;
            *"UserSync EventListener"*)
                echo -e "${PURPLE}[EXTENSION]${NC} $line"
                ;;
            *"Server started"*|*"started in"*)
                echo -e "${GREEN}[STARTUP]${NC} $line"
                ;;
            *)
                echo "$line"
                ;;
        esac
    done
}

function debug_keycloak() {
    local env=$1
    log_info "=== KEYCLOAK DEBUG REPORT ==="
    
    local container_name="keycloak-${env}"
    if [ "$env" = "prod" ]; then
        container_name="keycloak"
    fi
    
    # Check if container exists and is running
    if ! docker ps | grep -q "$container_name"; then
        log_error "Keycloak container '$container_name' is not running"
        if docker ps -a | grep -q "$container_name"; then
            log_info "Container exists but is stopped. Checking logs..."
            docker logs --tail 50 "$container_name"
        fi
        return 1
    fi
    
    log_success "Keycloak container is running"
    
    # Check mounted realm files
    log_info "Checking mounted realm files..."
    docker exec "$container_name" ls -la /opt/keycloak/data/import/ 2>/dev/null || {
        log_error "Cannot access /opt/keycloak/data/import/ directory"
    }
    
    # Check if realm file is valid JSON
    log_info "Validating realm JSON files..."
    docker exec "$container_name" sh -c 'for file in /opt/keycloak/data/import/*.json; do 
        echo "=== Checking $file ==="; 
        if python3 -m json.tool "$file" > /dev/null 2>&1; then 
            echo "✓ Valid JSON"; 
            head -5 "$file" | grep -E "\"realm\"|\"id\"" || echo "No realm name found in first 5 lines";
        else 
            echo "✗ Invalid JSON"; 
        fi; 
    done' 2>/dev/null
    
    # Check Keycloak providers
    log_info "Checking custom providers..."
    docker exec "$container_name" ls -la /opt/keycloak/providers/ 2>/dev/null || {
        log_warning "No custom providers directory or empty"
    }
    
    # Check if realm was imported by querying Keycloak
    log_info "Checking imported realms..."
    sleep 2
    
    # Try to get realm info
    local realm_check=$(docker exec "$container_name" curl -s http://localhost:8080/realms/stockfellow/.well-known/openid_configuration 2>/dev/null)
    if echo "$realm_check" | grep -q "stockfellow"; then
        log_success "Stockfellow realm is accessible"
        
        # Check for clients
        log_info "Checking realm clients via API..."
        # This would need admin credentials, so we'll check logs instead
        docker logs "$container_name" 2>&1 | grep -i "client" | tail -10
    else
        log_error "Stockfellow realm is not accessible"
        log_info "Available realms:"
        docker exec "$container_name" curl -s http://localhost:8080/realms/ 2>/dev/null | grep -o '"[^"]*"' || echo "Could not retrieve realms"
    fi
    
    # Show recent import-related logs
    log_info "Recent import-related logs:"
    docker logs --tail 100 "$container_name" 2>&1 | grep -i -E "(import|realm|client|event)" | tail -20
    
    log_info "=== END DEBUG REPORT ==="
}

function show_keycloak_logs() {
    local env=$1
    local container_name="keycloak-${env}"
    if [ "$env" = "prod" ]; then
        container_name="keycloak"
    fi
    
    log_info "Showing Keycloak logs with import/realm filtering..."
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    
    docker logs -f "$container_name" 2>&1 | grep -E "(import|realm|client|event|ERROR|WARN)" --color=always
}

function start_services() {
    local env=$1
    log_info "Starting $env environment..."
    
    ensure_executable
    
    case $env in
        "dev")
            docker-compose -f docker-compose.dev.yml up -d
            log_success "Development environment started!"
            log_info "Services available at:"
            echo "  - API Gateway: http://localhost:3000"
            echo "  - User Service: http://localhost:4020"
            echo "  - Group Service: http://localhost:4040"
            echo "  - Transaction Service: http://localhost:4080"
            echo "  - Notification Service: http://localhost:4050"
            echo "  - MFA Service: http://localhost:8087"
            echo "  - Keycloak: http://localhost:8080"
            echo "  - Adminer (DB Admin): http://localhost:8081"
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml up -d
            log_success "Production environment started!"
            log_info "Services available at:"
            echo "  - API Gateway: http://localhost:3000"
            echo "  - User Service: http://localhost:4020"
            echo "  - Group Service: http://localhost:4040"
            echo "  - Transaction Service: http://localhost:4080"
            echo "  - Notification Service: http://localhost:4050"
            echo "  - MFA Service: http://localhost:8087"
            echo "  - Keycloak: http://localhost:8080"
            echo "  - Resource Monitor: http://localhost:61208"
            # echo "  - Nginx: http://localhost:80"
            ;;
    esac
}

function stop_services() {
    local env=$1
    log_info "Stopping $env environment..."
    
    case $env in
        "dev")
            docker-compose -f docker-compose.dev.yml down
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml down
            ;;
    esac
    
    log_success "$env environment stopped"
}

function build_images() {
    local env=$1
    log_info "Building images for $env environment..."
    
    case $env in
        "dev")
            docker-compose -f docker-compose.dev.yml build --no-cache
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml build --no-cache
            ;;
    esac
    
    log_success "Images built successfully"
}

function show_logs() {
    local env=$1
    log_info "Showing logs for $env environment..."
    
    case $env in
        "dev")
            docker-compose -f docker-compose.dev.yml logs -f
            ;;
        "prod")
            docker-compose -f docker-compose.prod.yml logs -f
            ;;
    esac
}

function check_health() {
    local env=$1
    log_info "Checking health for $env environment..."
    
    local services=(
        "api-gateway:3000"
        "user-service:4020"
        "group-service:4040"
        "transaction-service:4080"
        "notification-service:4050"
        "mfa-service:8087"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -ra ADDR <<< "$service"
        local name=${ADDR[0]}
        local port=${ADDR[1]}
        
        if curl -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            log_success "$name is healthy"
        else
            log_error "$name is not healthy"
        fi
    done

    log_info "Checking Keycloak..."
    if curl -f http://localhost:8080/health/ready >/dev/null 2>&1; then
        log_success "Keycloak is ready"
        
        if curl -f http://localhost:8080/realms/stockfellow/.well-known/openid_configuration >/dev/null 2>&1; then
            log_success "Stockfellow realm is accessible"
        else
            log_warning "Stockfellow realm is not accessible"
        fi
    else
        log_error "Keycloak is not ready"
    fi
}

function clean_up() {
    local env=$1
    log_warning "This will remove all containers, volumes, and images for $env environment"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        case $env in
            "dev")
                docker-compose -f docker-compose.dev.yml down -v --remove-orphans
                ;;
            "prod")
                docker-compose -f docker-compose.prod.yml down -v --remove-orphans
                ;;
        esac
        
        log_info "Removing unused images..."
        docker system prune -f
        
        log_success "Cleanup completed"
    else
        log_info "Cleanup cancelled"
    fi
}

# Main script logic
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    print_usage
    exit 0
fi

if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    log_error "Invalid environment: $ENVIRONMENT"
    print_usage
    exit 1
fi

log_info "Environment: $ENVIRONMENT"
log_info "Action: $ACTION"

check_prerequisites

case $ACTION in
    "up")
        start_services $ENVIRONMENT
        ;;
    "down")
        stop_services $ENVIRONMENT
        ;;
    "build")
        build_images $ENVIRONMENT
        ;;
    "logs")
        show_logs $ENVIRONMENT
        ;;
    "health")
        check_health $ENVIRONMENT
        ;;
    "clean")
        clean_up $ENVIRONMENT
        ;;
    "keycloak-debug")
        debug_keycloak $ENVIRONMENT
        ;;
    "keycloak-logs")
        show_keycloak_logs $ENVIRONMENT
        ;;
    *)
        log_error "Invalid action: $ACTION"
        print_usage
        exit 1
        ;;
esac