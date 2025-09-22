#!/bin/bash

# Admin Service API Test Script with Token Authentication
# This script gets a token from Keycloak then tests all admin service endpoints

# Configuration
KEYCLOAK_URL="http://localhost:8080"
REALM="stockfellow"
CLIENT_ID="admin-service-client"
CLIENT_SECRET="r0cnwJ21La93AwhfOup3c9Ma4CtsLcXC"  # YOU MUST SET THIS - Get from Keycloak client credentials tab
BASE_URL="http://localhost:4060"
ADMIN_USER_ID="admin_user"

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

# Check if client secret is set
if [ -z "$CLIENT_SECRET" ]; then
    print_error "CLIENT_SECRET is not set!"
    echo "Please:"
    echo "1. Go to Keycloak Admin Console: http://localhost:8080"
    echo "2. Navigate to Clients > admin-service-client > Credentials tab"
    echo "3. Copy the Client Secret"
    echo "4. Set CLIENT_SECRET variable in this script"
    echo "   CLIENT_SECRET=\"your-secret-here\""
    exit 1
fi

# Function to get access token from Keycloak
get_access_token() {
    print_status "Getting access token from Keycloak..."
    
    token_response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=client_credentials" \
        -d "client_id=$CLIENT_ID" \
        -d "client_secret=$CLIENT_SECRET" \
        "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token")
    
    # Extract HTTP status code and response
    http_code=$(echo "$token_response" | tail -n1)
    response_body=$(echo "$token_response" | head -n -1)
    
    if [[ $http_code == "200" ]]; then
        access_token=$(echo "$response_body" | jq -r '.access_token // empty' 2>/dev/null)
        
        if [[ -n "$access_token" && "$access_token" != "null" ]]; then
            print_success "Access token obtained successfully"
            
            # Decode token to show content
            print_status "Token payload:"
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
    
    # Extract HTTP status code (last line)
    http_code=$(echo "$response" | tail -n1)
    # Extract response body (all lines except last)
    response_body=$(echo "$response" | head -n -1)
    
    echo "HTTP Status: $http_code"
    echo "Response: $response_body" | jq . 2>/dev/null || echo "Response: $response_body"
    
    if [[ $http_code -ge 200 && $http_code -lt 300 ]]; then
        print_success "Request successful"
    elif [[ $http_code -ge 400 && $http_code -lt 500 ]]; then
        if [[ $http_code == "403" ]]; then
            print_warning "Access forbidden - check if token has ADMIN role"
        elif [[ $http_code == "401" ]]; then
            print_warning "Unauthorized - token may be invalid or expired"
        elif [[ $http_code == "404" ]]; then
            print_warning "Resource not found (may be expected for test data)"
        else
            print_warning "Client error"
        fi
    else
        print_error "Server error"
    fi
    
    echo "----------------------------------------"
    echo
}

# Start testing
echo "=========================================="
echo "Admin Service API Test Suite with Auth"
echo "Keycloak: $KEYCLOAK_URL"
echo "Realm: $REALM"
echo "Client ID: $CLIENT_ID"
echo "Admin Service: $BASE_URL"
echo "=========================================="
echo

# Step 1: Get access token
if ! get_access_token; then
    print_error "Cannot proceed without valid access token"
    exit 1
fi

echo
echo "=========================================="
echo "Testing Admin Service Endpoints"
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

print_status "Results Summary:"
echo "- 200-299: Endpoints working correctly"
echo "- 401: Token invalid or expired"
echo "- 403: Token missing ADMIN role or access denied"
echo "- 404: Resource not found (expected for test data)"
echo "- 500: Server errors that need investigation"

echo
print_status "If you're getting 403 errors:"
echo "1. Verify your Keycloak client has 'Service accounts roles' enabled"
echo "2. Assign ADMIN realm role to the service account:"
echo "   - Go to Clients > admin-service-client > Service accounts roles"
echo "   - Click 'Assign role' and add 'ADMIN' role"
echo "3. Create ADMIN realm role if it doesn't exist:"
echo "   - Go to Realm roles > Create role > Name: 'ADMIN'"

echo
print_status "Token obtained and used for all requests"
print_status "Check the decoded token payload above to verify it contains required roles"