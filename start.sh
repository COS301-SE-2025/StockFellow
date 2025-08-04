#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if .env file exists
check_env_file() {
    if [ ! -f ".env" ]; then
        print_error ".env file not found!"
        print_step "Creating .env file from template..."
        # You would copy the template here
        print_warning "Please configure your .env file with appropriate values"
        return 1
    fi
    return 0
}

# Function to start core services only
start_core_services() {
    print_step "Starting core services (databases, redis, keycloak)..."
    docker-compose up -d \
        redis \
        postgres \
        notification-postgres \
        alfresco-postgres \
        activemq \
        keycloak
    
    print_step "Waiting for core services to be healthy..."
    sleep 10
}

# Function to start Alfresco (simplified - no transform services)
start_alfresco() {
    print_step "Starting Alfresco services..."
    docker-compose up -d \
        alfresco-search \
        alfresco-content-repository \
        alfresco-share
    
    print_step "Waiting for Alfresco to start..."
    sleep 30
}

# Function to start application services
start_app_services() {
    print_step "Starting application services..."
    docker-compose up -d \
        api-gateway \
        user-service \
        group-service \
        transaction-service \
        notification-service
}

# Function to show service status
show_status() {
    print_step "Service Status:"
    docker-compose ps
}

# Function to show service URLs
show_urls() {
    echo -e "\n${GREEN}=== Service URLs ===${NC}"
    echo "Keycloak Admin: http://localhost:8090"
    echo "API Gateway: http://localhost:3000"
    echo "User Service: http://localhost:4000"
    echo "Group Service: http://localhost:4040"
    echo "Transaction Service: http://localhost:4080"
    echo "Notification Service: http://localhost:4050"
    echo "Alfresco Repository: http://localhost:8080/alfresco"
    echo "Alfresco Share: http://localhost:8081/share"
    echo "Alfresco Search: http://localhost:8983/solr"
    echo "ActiveMQ Console: http://localhost:8161"
    echo -e "${GREEN}=====================${NC}\n"
}

# Main execution
main() {
    print_step "StockFellow Services Startup Script"
    
    # Check for .env file
    if ! check_env_file; then
        exit 1
    fi
    
    # Parse command line arguments
    case "${1:-full}" in
        "core")
            start_core_services
            ;;
        "alfresco")
            start_core_services
            start_alfresco
            ;;
        "app")
            start_core_services
            start_app_services
            ;;
        "full")
            start_core_services
            start_alfresco
            start_app_services
            ;;
        "status")
            show_status
            show_urls
            exit 0
            ;;
        "stop")
            print_step "Stopping all services..."
            docker-compose down
            exit 0
            ;;
        "clean")
            print_warning "This will remove all containers and volumes!"
            read -p "Are you sure? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                docker-compose down -v --remove-orphans
                docker system prune -f
            fi
            exit 0
            ;;
        *)
            echo "Usage: $0 [core|alfresco|app|full|status|stop|clean]"
            echo "  core     - Start only core services (DB, Redis, Keycloak)"
            echo "  alfresco - Start core + Alfresco services"
            echo "  app      - Start core + application services"
            echo "  full     - Start all services (default)"
            echo "  status   - Show service status and URLs"
            echo "  stop     - Stop all services"
            echo "  clean    - Stop and remove all containers/volumes"
            exit 1
            ;;
    esac
    
    show_status
    show_urls
    
    print_success "Services started successfully!"
    print_step "Use 'docker-compose logs -f [service-name]' to view logs"
    print_step "Use '$0 status' to check service status"
}

main "$@"