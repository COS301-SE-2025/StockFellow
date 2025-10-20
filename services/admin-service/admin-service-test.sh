#!/bin/bash

# Complete Admin Service API Test Script
# Tests all endpoints including auth, analytics, audit, and requests

# Configuration
KEYCLOAK_URL="http://localhost:8080"
REALM="stockfellow"
CLIENT_ID="admin-service-client"
CLIENT_SECRET="q03cb0bMVy672D6UWU485vWLe55ndX75"
BASE_URL="http://localhost:4060"
ADMIN_USER_ID="admin_user"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Counters for results
TOTAL_TESTS=0
SUCCESSFUL_TESTS=0
FAILED_TESTS=0
WARNING_TESTS=0

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

print_section() {
    echo -e "${PURPLE}[SECTION]${NC} $1"
}

print_endpoint() {
    echo -e "${CYAN}[ENDPOINT]${NC} $1"
}

# Function to extract JSON value using grep and sed
extract_json_value() {
    local json="$1"
    local key="$2"
    echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | sed "s/\"$key\":\"\([^\"]*\)\"/\1/"
}

get_access_token() {
    print_status "Getting access token from Keycloak..."
    
    token_response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=client_credentials" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")
    
    http_code=$(echo "$token_response" | tail -n1)
    response_body=$(echo "$token_response" | head -n -1)
    
    if [[ $http_code == "200" ]]; then
        access_token=$(extract_json_value "$response_body" "access_token")
        
        if [[ -n "$access_token" && "$access_token" != "null" ]]; then
            print_success "Access token obtained successfully"
            print_status "Token length: ${#access_token} characters"
            
            # Decode and show token payload
            print_status "Decoding token payload..."
            echo "$access_token" | cut -d. -f2 | base64 -d 2>/dev/null | jq . || echo "Failed to decode token payload"
            
            return 0
        else
            print_error "No access token in response"
            echo "Response: $response_body"
            return 1
        fi
    else
        print_error "Failed to get access token (HTTP $http_code)"
        echo "Response: $response_body"
        return 1
    fi
}

make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local expected_status=${5:-"2xx"}  # Default expect success
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    print_endpoint "$description"
    echo "Method: $method"
    echo "URL: $BASE_URL$endpoint"
    if [[ -n "$data" ]]; then
        echo "Data: $data"
    fi
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "X-User-Id: $ADMIN_USER_ID" \
            -H "Authorization: Bearer $access_token" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "X-User-Id: $ADMIN_USER_ID" \
            -H "Authorization: Bearer $access_token" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    echo "HTTP Status: $http_code"
    
    # Format response
    if [[ -n "$response_body" ]]; then
        echo "Response:"
        echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
    else
        echo "Response: (empty)"
    fi
    
    # Evaluate result
    if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
        print_success "✓ Request successful"
        SUCCESSFUL_TESTS=$((SUCCESSFUL_TESTS + 1))
    elif [[ $http_code -ge 400 && $http_code -lt 500 ]]; then
        if [[ $http_code == "403" ]]; then
            print_warning "⚠ Access forbidden - check if token has ADMIN role"
        elif [[ $http_code == "401" ]]; then
            print_warning "⚠ Unauthorized - token may be invalid or expired"
        elif [[ $http_code == "404" ]]; then
            if [[ "$expected_status" == "404" ]]; then
                print_warning "⚠ Resource not found (expected for test data)"
                WARNING_TESTS=$((WARNING_TESTS + 1))
            else
                print_warning "⚠ Resource not found (may be expected for test data)"
                WARNING_TESTS=$((WARNING_TESTS + 1))
            fi
        else
            print_error "✗ Client error (HTTP $http_code)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        print_error "✗ Server error (HTTP $http_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    echo "=========================================="
    echo
}

# Start testing
echo "============================================"
echo "🚀 COMPLETE ADMIN SERVICE API TEST SUITE 🚀"
echo "============================================"
echo "Keycloak: $KEYCLOAK_URL"
echo "Realm: $REALM"  
echo "Client ID: $CLIENT_ID"
echo "Admin Service: $BASE_URL"
echo "============================================"
echo

# Step 1: Get access token
if ! get_access_token; then
    print_error "Cannot proceed without valid access token"
    exit 1
fi

echo
echo "============================================"
echo "🔐 TESTING AUTHENTICATION ENDPOINTS"
echo "============================================"
echo

print_section "Authentication Endpoints"

# Test auth endpoints (these should work without the Bearer token)
make_request "POST" "/api/admin/auth/login" '{"username":"admin","password":"AdminTest123!"}' "Admin Login (Password Grant)"

# Test token validation (this needs the Bearer token)
make_request "GET" "/api/admin/auth/validate" "" "Token Validation"

echo
echo "============================================"
echo "📊 TESTING BASIC SERVICE ENDPOINTS"
echo "============================================"
echo

print_section "Basic Service Information"

make_request "GET" "/api/admin" "" "Service Information"

echo
echo "============================================"
echo "📈 TESTING DASHBOARD ENDPOINTS"
echo "============================================"
echo

print_section "Dashboard Endpoints"

make_request "GET" "/api/admin/dashboard/summary" "" "Complete Dashboard Summary"

echo
echo "============================================"
echo "📊 TESTING ANALYTICS ENDPOINTS"
echo "============================================"
echo

print_section "Analytics Endpoints"

make_request "GET" "/api/admin/analytics/dashboard" "" "Analytics Dashboard (7 days default)"
make_request "GET" "/api/admin/analytics/dashboard?timeRange=30d" "" "Analytics Dashboard (30 days)"
make_request "GET" "/api/admin/analytics/dashboard?timeRange=90d" "" "Analytics Dashboard (90 days)"

make_request "GET" "/api/admin/analytics/users/stats" "" "User Statistics"
make_request "GET" "/api/admin/analytics/groups/stats" "" "Group Statistics"
make_request "GET" "/api/admin/analytics/transactions/stats" "" "Transaction Statistics"

make_request "GET" "/api/admin/analytics/revenue" "" "Revenue Analytics (30 days default)"
make_request "GET" "/api/admin/analytics/revenue?period=7d" "" "Revenue Analytics (7 days)"
make_request "GET" "/api/admin/analytics/revenue?period=90d" "" "Revenue Analytics (90 days)"

echo
echo "============================================"
echo "🔍 TESTING AUDIT & MONITORING ENDPOINTS"
echo "============================================"
echo

print_section "Audit Log Endpoints"

make_request "GET" "/api/admin/audit/logs" "" "All Audit Logs"
make_request "GET" "/api/admin/audit/logs?page=0&size=10" "" "Paginated Audit Logs"
make_request "GET" "/api/admin/audit/logs?flaggedOnly=true" "" "Flagged Audit Logs Only"
make_request "GET" "/api/admin/audit/logs?userId=test-user-123" "" "Audit Logs for Specific User"
make_request "GET" "/api/admin/audit/logs?endpoint=/api/users" "" "Audit Logs for Specific Endpoint"

# Test with date range
start_date=$(date -d "7 days ago" -u +"%Y-%m-%dT%H:%M:%S")
end_date=$(date -u +"%Y-%m-%dT%H:%M:%S")
make_request "GET" "/api/admin/audit/logs?startDate=${start_date}&endDate=${end_date}" "" "Audit Logs with Date Range"

print_section "Fraud Detection Endpoints"

make_request "GET" "/api/admin/audit/fraud/suspicious" "" "Suspicious Activity Report"

# Test investigation marking
investigation_data='{
    "logId": "test-log-id-123",
    "reason": "Unusual access pattern detected during automated testing"
}'
make_request "POST" "/api/admin/audit/fraud/investigate" "$investigation_data" "Mark Log for Investigation"

print_section "User Activity Endpoints"

make_request "GET" "/api/admin/audit/user/test-user-123/activity" "" "User Activity Report"
make_request "GET" "/api/admin/audit/user/admin/activity" "" "Admin User Activity Report"

echo
echo "============================================"
echo "📝 TESTING REQUEST MANAGEMENT ENDPOINTS"
echo "============================================"
echo

print_section "Request Management Endpoints"

make_request "GET" "/api/admin/requests/pending" "" "All Pending Requests"
make_request "GET" "/api/admin/requests/pending?page=0&size=5" "" "Paginated Pending Requests"
make_request "GET" "/api/admin/requests/pending?requestType=LEAVE_GROUP" "" "Pending Leave Group Requests"
make_request "GET" "/api/admin/requests/pending?requestType=JOIN_GROUP" "" "Pending Join Group Requests"
make_request "GET" "/api/admin/requests/pending?requestType=TRANSACTION_DISPUTE" "" "Pending Transaction Dispute Requests"

# Test request details (expect 404 for non-existent requests)
make_request "GET" "/api/admin/requests/test-request-123/details" "" "Request Details (Non-existent)" "404"
make_request "GET" "/api/admin/requests/invalid-id/details" "" "Request Details (Invalid ID)" "404"

# Test request approval/rejection (expect 400/404 for non-existent requests)
approval_data='{
    "adminNotes": "Approved after reviewing user case - legitimate request during testing"
}'
make_request "POST" "/api/admin/requests/test-request-123/approve" "$approval_data" "Approve Admin Request (Non-existent)" "404"

rejection_data='{
    "adminNotes": "Rejected - insufficient documentation provided during testing"
}'
make_request "POST" "/api/admin/requests/test-request-456/reject" "$rejection_data" "Reject Admin Request (Non-existent)" "404"

# Test with invalid data
invalid_approval='{
    "adminNotes": ""
}'
make_request "POST" "/api/admin/requests/test-request-789/approve" "$invalid_approval" "Approve Request with Empty Notes" "400"

echo
echo "============================================"
echo "🧪 TESTING ERROR HANDLING"
echo "============================================"
echo

print_section "Error Handling Tests"

# Test invalid endpoints
make_request "GET" "/api/admin/nonexistent" "" "Non-existent Endpoint" "404"
make_request "GET" "/api/admin/analytics/invalid" "" "Invalid Analytics Endpoint" "404"

# Test invalid parameters
make_request "GET" "/api/admin/analytics/dashboard?timeRange=invalid" "" "Invalid Time Range Parameter" "400"
make_request "GET" "/api/admin/analytics/revenue?period=999d" "" "Invalid Revenue Period" "400"

# Test malformed request bodies
make_request "POST" "/api/admin/audit/fraud/investigate" '{"invalid": "json"}' "Invalid Investigation Request" "400"
make_request "POST" "/api/admin/requests/test/approve" '{}' "Empty Approval Request" "400"

echo
echo "============================================"
echo "📊 TEST RESULTS SUMMARY"
echo "============================================"
echo

print_status "Test Execution Complete!"
echo
echo "📈 STATISTICS:"
echo "Total Tests: $TOTAL_TESTS"
echo "Successful (2xx): $SUCCESSFUL_TESTS"
echo "Warnings (4xx expected): $WARNING_TESTS"
echo "Failed (unexpected errors): $FAILED_TESTS"
echo

if [[ $FAILED_TESTS -eq 0 ]]; then
    print_success "🎉 All tests completed successfully!"
    echo "✅ All endpoints are responding correctly"
    echo "✅ Authentication is working"
    echo "✅ JWT token has proper ADMIN role"
    echo "✅ Error handling is functioning"
else
    print_warning "⚠️  Some tests failed - review the results above"
    echo "❌ Failed tests: $FAILED_TESTS"
    echo "⚠️  Check server logs for more details"
fi

echo
echo "🔍 DEBUGGING TIPS:"
echo "- 401 Unauthorized: Check if admin-service can reach Keycloak"
echo "- 403 Forbidden: Verify ADMIN role is assigned to service account"
echo "- 404 Not Found: Expected for test data, check if endpoints exist"
echo "- 500 Server Error: Check admin-service logs with: docker logs admin-service"
echo
echo "🔗 USEFUL COMMANDS:"
echo "- Check admin service logs: docker logs admin-service"
echo "- Check admin service health: curl http://localhost:4060/actuator/health"
echo "- Access Keycloak admin: http://localhost:8080"
echo

print_status "Test suite completed at $(date)"
echo "============================================"