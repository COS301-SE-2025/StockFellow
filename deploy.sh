#!/bin/bash
# deploy.sh - Environment deployment helper script

set -e

ENVIRONMENT=${1:-"dev"}
ACTION=${2:-"up"}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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
        log_info "Please ensure the init-multiple-databases.sh script is in ./database/ directory"
    fi
    
    # Check if .env file exists
    if [ ! -f ".env" ]; then
        log_warning ".env file not found. Creating sample..."
        cat > .env << EOF
# Sample environment variables
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=postgres

KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

REDIS_PASSWORD=

# Add other required environment variables here
EOF
        log_info "Sample .env file created. Please configure it with your values."
    fi
    
    log_success "Prerequisites check completed"
}

function ensure_executable() {
    if [ -f "./database/init-multiple-databases.sh" ]; then
        chmod +x ./database/init-multiple-databases.sh
        log_info "Made database init script executable"
    fi
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
    *)
        log_error "Invalid action: $ACTION"
        print_usage
        exit 1
        ;;
esac