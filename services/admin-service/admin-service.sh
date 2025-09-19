#!/bin/bash

# Admin Service API Test Script
# This script tests all endpoints of the admin service

# Configuration
BASE_URL="http://localhost:8080"  # Change this to your actual service URL
ADMIN_USER_ID="admin-user-123"
SESSION_ID="admin-session-456"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to make authenticated requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    print_status "Testing: $description"
    echo "Endpoint: $method $BASE_URL$endpoint"
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "X-User-Id: $ADMIN_USER_ID" \
            -H "X-Session-Id: $SESSION_ID" \
            -H "Authorization: Bearer fake-admin-jwt-token" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "X-User-Id: $ADMIN_USER_ID" \
            -H "X-Session-Id: $SESSION_ID" \
            -H "Authorization: Bearer fake-admin-jwt-token" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    # Extract HTTP status code (last line)
    http_code=$(echo "$response" | tail -n1)
    # Extract response body (all lines except last)
    response_body=$(echo "$response" | head -n -1)
    
    echo "HTTP Status: $http_code"
    echo "Response: $response_body" | jq . 2>/dev/null || echo "Response: $response_body"
    
    if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
        print_success "Request successful"
    elif [[ $http_code -ge 400 && $http_code -lt 500 ]]; then
        print_warning "Client error (might be expected for auth/validation)"
    else
        print_error "Server error"
    fi
    
    echo "----------------------------------------"
    echo
}

# Start testing
echo "=========================================="
echo "Admin Service API Test Suite"
echo "Base URL: $BASE_URL"
echo "Admin User ID: $ADMIN_USER_ID"
echo "=========================================="
echo

# Test 1: Service Info
make_request "GET" "/api/admin" "" "Service Info"

# Test 2: Dashboard Summary
make_request "GET" "/api/admin/dashboard/summary" "" "Admin Dashboard Summary"

# Test 3: Analytics Dashboard
make_request "GET" "/api/admin/analytics/dashboard" "" "Analytics Dashboard (7 days default)"
make_request "GET" "/api/admin/analytics/dashboard?timeRange=30d" "" "Analytics Dashboard (30 days)"

# Test 4: User Statistics
make_request "GET" "/api/admin/analytics/users/stats" "" "User Statistics"

# Test 5: Group Statistics
make_request "GET" "/api/admin/analytics/groups/stats" "" "Group Statistics"

# Test 6: Transaction Statistics
make_request "GET" "/api/admin/analytics/transactions/stats" "" "Transaction Statistics"

# Test 7: Revenue Analytics
make_request "GET" "/api/admin/analytics/revenue" "" "Revenue Analytics (30 days default)"
make_request "GET" "/api/admin/analytics/revenue?period=90d" "" "Revenue Analytics (90 days)"

# Test 8: Audit Logs
make_request "GET" "/api/admin/audit/logs" "" "All Audit Logs"
make_request "GET" "/api/admin/audit/logs?flaggedOnly=true" "" "Flagged Audit Logs Only"
make_request "GET" "/api/admin/audit/logs?userId=test-user-123" "" "Audit Logs for Specific User"
make_request "GET" "/api/admin/audit/logs?endpoint=/api/users" "" "Audit Logs for Specific Endpoint"

# Test 9: Suspicious Activity
make_request "GET" "/api/admin/audit/fraud/suspicious" "" "Suspicious Activity Report"

# Test 10: User Activity
make_request "GET" "/api/admin/audit/user/test-user-123/activity" "" "User Activity Report"

# Test 11: Mark Log for Investigation
investigation_data='{
    "logId": "test-log-id-123",
    "reason": "Unusual access pattern detected during testing"
}'
make_request "POST" "/api/admin/audit/fraud/investigate" "$investigation_data" "Mark Log for Investigation"

# Test 12: Pending Admin Requests
make_request "GET" "/api/admin/requests/pending" "" "All Pending Requests"
make_request "GET" "/api/admin/requests/pending?requestType=LEAVE_GROUP" "" "Pending Leave Group Requests"

# Test 13: Request Details
make_request "GET" "/api/admin/requests/test-request-123/details" "" "Request Details"

# Test 14: Approve Request
approval_data='{
    "adminNotes": "Approved after reviewing user case - legitimate request"
}'
make_request "POST" "/api/admin/requests/test-request-123/approve" "$approval_data" "Approve Admin Request"

# Test 15: Reject Request  
rejection_data='{
    "adminNotes": "Rejected - insufficient documentation provided"
}'
make_request "POST" "/api/admin/requests/test-request-456/reject" "$rejection_data" "Reject Admin Request"

echo "=========================================="
echo "Test Suite Completed"
echo "=========================================="
echo
print_status "Notes:"
echo "- Many requests may return 401/403 errors due to missing proper JWT authentication"
echo "- Some endpoints may return 404 errors for non-existent resources (expected)"
echo "- 500 errors might indicate actual service issues that need investigation"
echo "- Success responses (200-299) indicate the endpoints are properly configured"
echo
print_warning "To run with proper authentication:"
echo "1. Replace 'fake-admin-jwt-token' with a real JWT token"
echo "2. Ensure your JWT token has ADMIN role"
echo "3. Configure proper CORS and security settings"
echo
print_status "Sample usage with real auth:"
echo 'export ADMIN_JWT="your-real-jwt-token-here"'
echo 'sed -i "s/fake-admin-jwt-token/\$ADMIN_JWT/g" this-script.sh'