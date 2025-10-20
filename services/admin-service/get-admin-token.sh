#!/bin/bash

# Script to register an admin user and get authentication token
BASE_URL="http://10.0.2.2:3000/api"
ADMIN_USERNAME="admin_test_user"
ADMIN_PASSWORD="AdminPass123!"
ADMIN_EMAIL="admin@stockfellow.com"

echo "=========================================="
echo "Getting Admin Token for Admin Service"
echo "=========================================="

# Step 1: Register admin user (if not already exists)
echo "Step 1: Registering admin user..."

register_response=$(curl -s -w "\n%{http_code}" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "username": "'$ADMIN_USERNAME'",
    "password": "'$ADMIN_PASSWORD'",
    "email": "'$ADMIN_EMAIL'",
    "firstName": "Admin",
    "lastName": "User"
  }' \
  "$BASE_URL/register")

# Extract HTTP status code and response
register_http_code=$(echo "$register_response" | tail -n1)
register_body=$(echo "$register_response" | head -n -1)

echo "Registration Response Code: $register_http_code"
echo "Registration Response: $register_body" | jq . 2>/dev/null || echo "Registration Response: $register_body"

if [[ $register_http_code == "200" || $register_http_code == "409" ]]; then
  echo "✓ User registration successful or user already exists"
  
  # Extract user ID if available
  user_id=$(echo "$register_body" | jq -r '.userId // empty' 2>/dev/null)
  if [[ -n "$user_id" && "$user_id" != "null" ]]; then
    echo "✓ User ID: $user_id"
  else
    echo "! User ID not returned in registration response"
  fi
else
  echo "✗ Registration failed"
fi

echo
echo "----------------------------------------"
echo

# Step 2: Try to get authentication token
echo "Step 2: Attempting to get authentication token..."

# Check if there's a login endpoint
echo "Trying login endpoint..."

login_response=$(curl -s -w "\n%{http_code}" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "username": "'$ADMIN_USERNAME'",
    "password": "'$ADMIN_PASSWORD'"
  }' \
  "$BASE_URL/login" 2>/dev/null)

login_http_code=$(echo "$login_response" | tail -n1)
login_body=$(echo "$login_response" | head -n -1)

echo "Login Response Code: $login_http_code"
echo "Login Response: $login_body" | jq . 2>/dev/null || echo "Login Response: $login_body"

if [[ $login_http_code == "200" ]]; then
  echo "✓ Login successful!"
  
  # Extract token and user ID
  access_token=$(echo "$login_body" | jq -r '.access_token // .token // .accessToken // empty' 2>/dev/null)
  user_id=$(echo "$login_body" | jq -r '.userId // .user_id // .id // empty' 2>/dev/null)
  
  if [[ -n "$access_token" && "$access_token" != "null" ]]; then
    echo "✓ Access Token obtained!"
    echo "✓ User ID: $user_id"
    
    echo
    echo "=========================================="
    echo "SUCCESS - Use these values:"
    echo "=========================================="
    echo "Access Token: $access_token"
    echo "User ID: $user_id"
    echo
    echo "Test the admin service with:"
    echo "curl -H 'Authorization: Bearer $access_token' \\"
    echo "     -H 'X-User-Id: $user_id' \\"
    echo "     http://localhost:4060/api/admin"
    
    # Decode token to check content
    echo
    echo "Token Analysis:"
    echo "Header:" 
    echo $access_token | cut -d. -f1 | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed to decode"
    echo
    echo "Payload:"
    echo $access_token | cut -d. -f2 | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed to decode"
    
  else
    echo "✗ No access token in response"
  fi
else
  echo "✗ Login failed or login endpoint not available"
fi

echo
echo "=========================================="
echo "Alternative approaches if login failed:"
echo "=========================================="
echo
echo "1. Try auth endpoint:"
echo "curl -X POST $BASE_URL/auth -d '{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}'"
echo
echo "2. Try authenticate endpoint:"
echo "curl -X POST $BASE_URL/authenticate -d '{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}'"
echo
echo "3. Check available endpoints:"
echo "curl $BASE_URL/"
echo
echo "4. Direct Keycloak token (if you have client details):"
echo "curl -X POST http://localhost:8080/realms/stockfellow/protocol/openid-connect/token \\"
echo "  -d 'grant_type=password&client_id=YOUR_CLIENT&username=$ADMIN_USERNAME&password=$ADMIN_PASSWORD'"

echo
echo "Note: Make sure your admin user has ADMIN role assigned in Keycloak!"