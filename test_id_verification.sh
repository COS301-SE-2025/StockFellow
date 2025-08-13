#!/bin/bash

# ID Verification System Test Script
# This script tests the ID verification endpoint with various scenarios

set -e  # Exit on error

# Configuration
BASE_URL="http://localhost:3000"  # API Gateway URL
USER_SERVICE_URL="http://localhost:4000"  # Direct user service URL for testing
TEST_DIR="$(dirname "$0")/test_files"
RESULTS_DIR="$(dirname "$0")/test_results"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Create directories
mkdir -p "$TEST_DIR" "$RESULTS_DIR"

# Function to print colored output
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

# Function to create test PDF files
create_test_files() {
    print_status "INFO" "Creating test PDF files..."
    
    # Create a simple PDF with SA ID number using echo and pandoc (if available)
    # Or create text files that simulate PDF content for testing
    
    cat > "$TEST_DIR/valid_id_document.txt" << 'EOF'
REPUBLIC OF SOUTH AFRICA
IDENTITY DOCUMENT

Full Names: JOHN DOE
Identity Number: 8001015009087
Date of Birth: 01 JAN 1980
Sex: Male
Country of Birth: RSA

This is a test document for ID verification.
EOF

    cat > "$TEST_DIR/invalid_id_document.txt" << 'EOF'
REPUBLIC OF SOUTH AFRICA
IDENTITY DOCUMENT

Full Names: JANE DOE  
Identity Number: 1234567890123
Date of Birth: 01 JAN 1990
Sex: Female
Country of Birth: RSA

This document contains an invalid ID number.
EOF

    cat > "$TEST_DIR/no_id_document.txt" << 'EOF'
SOME RANDOM DOCUMENT

This document does not contain any South African ID number.
Just some random text for testing purposes.
EOF

    # Convert text files to PDF using pandoc if available, otherwise use simple method
    if command -v pandoc &> /dev/null; then
        pandoc "$TEST_DIR/valid_id_document.txt" -o "$TEST_DIR/valid_id_document.pdf"
        pandoc "$TEST_DIR/invalid_id_document.txt" -o "$TEST_DIR/invalid_id_document.pdf" 
        pandoc "$TEST_DIR/no_id_document.txt" -o "$TEST_DIR/no_id_document.pdf"
        print_status "SUCCESS" "PDF files created using pandoc"
    else
        print_status "WARNING" "pandoc not found. Using text files for testing."
        # Create dummy PDF files (just rename .txt to .pdf for basic testing)
        cp "$TEST_DIR/valid_id_document.txt" "$TEST_DIR/valid_id_document.pdf"
        cp "$TEST_DIR/invalid_id_document.txt" "$TEST_DIR/invalid_id_document.pdf"
        cp "$TEST_DIR/no_id_document.txt" "$TEST_DIR/no_id_document.pdf"
    fi
}

# Function to wait for services to be ready
wait_for_services() {
    print_status "INFO" "Waiting for services to be ready..."
    
    local services=("$BASE_URL" "$USER_SERVICE_URL")
    local service_names=("API Gateway" "User Service")
    
    for i in "${!services[@]}"; do
        local url="${services[$i]}"
        local name="${service_names[$i]}"
        local retries=30
        
        print_status "INFO" "Checking $name at $url..."
        
        while [ $retries -gt 0 ]; do
            if curl -s --fail "$url/actuator/health" > /dev/null 2>&1 || \
               curl -s --fail "$url/api/users" > /dev/null 2>&1; then
                print_status "SUCCESS" "$name is ready"
                break
            fi
            
            retries=$((retries - 1))
            if [ $retries -eq 0 ]; then
                print_status "FAILURE" "$name is not responding after 5 minutes"
                exit 1
            fi
            
            sleep 10
        done
    done
}

# Function to get authentication token (mock for testing)
get_auth_token() {
    # This is a mock function - in real implementation, you would authenticate with Keycloak
    echo "mock-jwt-token"
}

# Function to run a test
run_test() {
    local test_name=$1
    local expected_status=$2
    local file_path=$3
    local user_id=$4
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    print_status "INFO" "Running test: $test_name"
    
    # Get auth token
    local auth_token=$(get_auth_token)
    
    # Make the API call
    local response_file="$RESULTS_DIR/${test_name// /_}_response.json"
    local status_code
    
    if [ -f "$file_path" ]; then
        status_code=$(curl -s -w "%{http_code}" \
            -X POST \
            -H "Authorization: Bearer $auth_token" \
            -H "X-User-Id: $user_id" \
            -H "X-User-Name: testuser" \
            -F "file=@$file_path" \
            -F "userId=$user_id" \
            "$USER_SERVICE_URL/api/users/verifyID" \
            -o "$response_file")
    else
        # Test with no file
        status_code=$(curl -s -w "%{http_code}" \
            -X POST \
            -H "Authorization: Bearer $auth_token" \
            -H "X-User-Id: $user_id" \
            -H "X-User-Name: testuser" \
            -F "userId=$user_id" \
            "$USER_SERVICE_URL/api/users/verifyID" \
            -o "$response_file")
    fi
    
    # Check result
    if [ "$status_code" -eq "$expected_status" ]; then
        print_status "SUCCESS" "Test '$test_name' passed (Status: $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Print response for successful tests
        if [ "$status_code" -eq 200 ]; then
            echo "Response:"
            cat "$response_file" | jq '.' 2>/dev/null || cat "$response_file"
            echo ""
        fi
    else
        print_status "FAILURE" "Test '$test_name' failed (Expected: $expected_status, Got: $status_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "Response:"
        cat "$response_file"
        echo ""
    fi
}

# Function to test service availability
test_service_availability() {
    print_status "INFO" "Testing service availability..."
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    local response=$(curl -s "$USER_SERVICE_URL/api/users")
    if echo "$response" | grep -q "User Service"; then
        print_status "SUCCESS" "User service is responding correctly"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        print_status "FAILURE" "User service is not responding correctly"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Function to run all ID verification tests
run_id_verification_tests() {
    print_status "INFO" "Starting ID verification tests..."
    
    local test_user_id="test-user-$(date +%s)"
    
    # Test 1: Valid ID document
    run_test "Valid ID Document" 200 "$TEST_DIR/valid_id_document.pdf" "$test_user_id"
    
    # Test 2: Invalid ID document  
    run_test "Invalid ID Document" 400 "$TEST_DIR/invalid_id_document.pdf" "$test_user_id"
    
    # Test 3: Document with no ID
    run_test "No ID in Document" 400 "$TEST_DIR/no_id_document.pdf" "$test_user_id"
    
    # Test 4: No file provided
    run_test "No File Provided" 400 "" "$test_user_id"
    
    # Test 5: No user ID provided
    run_test "No User ID" 401 "$TEST_DIR/valid_id_document.pdf" ""
    
    # Test 6: Wrong file type (if we have a text file)
    if [ -f "$TEST_DIR/valid_id_document.txt" ]; then
        run_test "Wrong File Type" 400 "$TEST_DIR/valid_id_document.txt" "$test_user_id"
    fi
}

# Function to test direct PDF processing (unit test style)
test_pdf_processing() {
    print_status "INFO" "Testing PDF processing logic..."
    
    # This would require direct access to the service classes
    # For now, we'll test through the API endpoint
    print_status "INFO" "PDF processing tests integrated with API tests"
}

# Function to print test summary
print_summary() {
    echo ""
    echo "========================================"
    echo "           TEST SUMMARY"
    echo "========================================"
    echo "Total Tests: $TOTAL_TESTS"
    echo "Passed: $PASSED_TESTS"
    echo "Failed: $FAILED_TESTS"
    echo ""
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_status "SUCCESS" "All tests passed!"
        exit 0
    else
        print_status "FAILURE" "$FAILED_TESTS test(s) failed"
        exit 1
    fi
}

# Function to cleanup test files
cleanup() {
    print_status "INFO" "Cleaning up test files..."
    rm -rf "$TEST_DIR" "$RESULTS_DIR"
    print_status "SUCCESS" "Cleanup completed"
}

# Main execution
main() {
    echo "========================================"
    echo "    ID VERIFICATION SYSTEM TESTS"
    echo "========================================"
    echo ""
    
    # Check dependencies
    if ! command -v curl &> /dev/null; then
        print_status "FAILURE" "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_status "WARNING" "jq not found - JSON responses will not be formatted"
    fi
    
    # Create test files
    create_test_files
    
    # Wait for services
    wait_for_services
    
    # Run tests
    test_service_availability
    run_id_verification_tests
    test_pdf_processing
    
    # Print summary
    print_summary
}

# Handle script arguments
case "${1:-}" in
    "cleanup")
        cleanup
        exit 0
        ;;
    "create-files")
        create_test_files
        print_status "SUCCESS" "Test files created in $TEST_DIR"
        exit 0
        ;;
    "wait")
        wait_for_services
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac