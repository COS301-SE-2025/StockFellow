#!/bin/bash
# local-ci.sh - Run the same checks as GitHub Actions locally

set -e  # Exit on any error

echo "🔍 Starting Local CI Checks..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is running (for later steps)
if ! docker info &> /dev/null; then
    print_warning "Docker is not running. Some tests may fail."
fi

echo "📋 Found services:"
find services -name "pom.xml" -type f | sed 's|/pom.xml||' | sed 's|services/||'

echo ""
echo "🔨 Step 1: Compilation Check (mirrors GitHub Actions quick validation)"
echo "=================================================="

# Mirror the exact compilation check from your GitHub Actions
compilation_failed=false
for service_dir in services/*/; do
    if [ -f "$service_dir/pom.xml" ]; then
        service_name=$(basename "$service_dir")
        echo "Compiling $service_name..."
        
        cd "$service_dir"
        if mvn compile -DskipTests -T 2C -q; then
            print_status "$service_name compiled successfully"
        else
            print_error "$service_name compilation failed"
            compilation_failed=true
        fi
        cd - > /dev/null
    fi
done

if [ "$compilation_failed" = true ]; then
    print_error "Compilation failed! Fix these issues before pushing."
    exit 1
fi

echo ""
echo "🧪 Step 2: Test Dependencies Check"
echo "=================================="

# Check for common missing dependencies
for service_dir in services/*/; do
    if [ -f "$service_dir/pom.xml" ]; then
        service_name=$(basename "$service_dir")
        
        # Check if JaCoCo plugin exists when jacoco:report is called in CI
        if grep -r "jacoco:report" .github/workflows/ &> /dev/null; then
            if ! grep -q "jacoco-maven-plugin" "$service_dir/pom.xml"; then
                print_warning "$service_name: Missing JaCoCo plugin but CI tries to run jacoco:report"
            fi
        fi
        
        # Check for missing @Value import if @Value annotation is used
        find "$service_dir/src" -name "*.java" -exec grep -l "@Value" {} \; 2>/dev/null | while read -r file; do
            if ! grep -q "import org.springframework.beans.factory.annotation.Value" "$file"; then
                print_warning "$service_name: $file uses @Value but missing import"
            fi
        done
    fi
done

echo ""
echo "🐳 Step 3: Docker Build Test (optional)"
echo "======================================="

build_docker_images=false
read -p "Build Docker images to test containerization? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    build_docker_images=true
fi

if [ "$build_docker_images" = true ]; then
    for service_dir in services/*/; do
        if [ -f "$service_dir/Dockerfile" ]; then
            service_name=$(basename "$service_dir")
            echo "Building Docker image for $service_name..."
            
            cd "$service_dir"
            if docker build -t "stockfellow-$service_name:test" .; then
                print_status "$service_name Docker image built successfully"
            else
                print_error "$service_name Docker build failed"
            fi
            cd - > /dev/null
        fi
    done
fi

echo ""
echo "📝 Step 4: Configuration Validation"
echo "==================================="

# Check for common configuration issues
if [ -f ".github/workflows/ci.yml" ] || [ -f ".github/workflows/main.yml" ]; then
    echo "Validating GitHub Actions workflow..."
    
    # Check if all services in docker-compose are also in CI matrix
    if [ -f "docker-compose.prod.yml" ]; then
        compose_services=$(grep -E "^\s+[a-zA-Z0-9-]+:" docker-compose.prod.yml | grep -v "postgres\|redis\|keycloak\|activemq\|resource-monitor" | sed 's/://g' | sed 's/^[[:space:]]*//')
        
        for service in $compose_services; do
            if ! grep -r "matrix:" .github/workflows/ | grep -q "$service"; then
                print_warning "Service '$service' in docker-compose.prod.yml but not in CI matrix"
            fi
        done
    fi
fi

echo ""
echo "🔍 Step 5: Pre-commit Simulation"
echo "==============================="

# Run a subset of what would run in CI
echo "Running quick test suite..."

test_failed=false
for service_dir in services/*/; do
    if [ -f "$service_dir/pom.xml" ]; then
        service_name=$(basename "$service_dir")
        echo "Testing $service_name (unit tests only)..."
        
        cd "$service_dir"
        if mvn test -Dtest="**/*Test.java" -DfailIfNoTests=false -q; then
            print_status "$service_name tests passed"
        else
            print_error "$service_name tests failed"
            test_failed=true
        fi
        cd - > /dev/null
    fi
done

echo ""
echo "📊 Summary"
echo "=========="

if [ "$compilation_failed" = true ] || [ "$test_failed" = true ]; then
    print_error "Local CI checks failed! Please fix the issues before pushing."
    echo ""
    echo "Common fixes:"
    echo "• Add missing imports (especially @Value)"
    echo "• Add missing Maven plugins (JaCoCo, etc.)"
    echo "• Fix compilation errors"
    echo "• Ensure all tests pass"
    exit 1
else
    print_status "All local CI checks passed! Ready to push."
    echo ""
    echo "Next steps:"
    echo "• git add ."
    echo "• git commit -m 'Your message'"
    echo "• git push"
fi