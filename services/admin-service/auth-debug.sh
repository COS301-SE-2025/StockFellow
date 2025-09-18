#!/bin/bash

# Authentication Debug Script for Admin Service
# This script helps diagnose JWT authentication issues

BASE_URL="http://localhost:4060"
JWT_TOKEN="eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjNTRkNWViYS04NjM1LTQyNDAtODI0ZC1iOTgwYzg0NDRhNGIifQ.eyJleHAiOjE3NTgxNDE2NDIsImlhdCI6MTc1ODA1NTI0MiwianRpIjoiMTQ5NjdmYWItYjA3MC00MWExLTgyY2YtYTY3MDRlNmM3M2JkIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MTgwL3JlYWxtcy9zdG9ja2ZlbGxvdyIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9yZWFsbXMvc3RvY2tmZWxsb3ciLCJ0eXAiOiJJbml0aWFsQWNjZXNzVG9rZW4ifQ.UXei9tKO2QNCkbSwnk0IOzlKkyezedqE3kYPb-q505b0EdXzicWJv4tGoJvbclq_Z_9EDdeQmdv27Sa6BJ1DTA"

echo "=========================================="
echo "JWT Authentication Debug"
echo "=========================================="

echo "1. Decoding JWT token to check content..."
echo "JWT Header:"
echo $JWT_TOKEN | cut -d. -f1 | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed to decode header"

echo
echo "JWT Payload:"
echo $JWT_TOKEN | cut -d. -f2 | base64 -d 2>/dev/null | jq . 2>/dev/null || echo "Failed to decode payload"

echo
echo "2. Testing different authentication methods..."

echo
echo "Test 1: With Authorization header only"
curl -s -w "\nHTTP Status: %{http_code}\n" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  "$BASE_URL/api/admin"

echo
echo "Test 2: With Authorization + Content-Type"
curl -s -w "\nHTTP Status: %{http_code}\n" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  "$BASE_URL/api/admin"

echo
echo "Test 3: With all headers"
curl -s -w "\nHTTP Status: %{http_code}\n" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admin_user" \
  -H "X-Session-Id: admin-session-456" \
  "$BASE_URL/api/admin"

echo
echo "Test 4: Check if actuator endpoints work (usually bypass security)"
curl -s -w "\nHTTP Status: %{http_code}\n" \
  "$BASE_URL/actuator/health" 2>/dev/null || echo "Actuator not available"

echo
echo "Test 5: Test without authentication (should also give 403)"
curl -s -w "\nHTTP Status: %{http_code}\n" \
  "$BASE_URL/api/admin"

echo
echo "=========================================="
echo "Debugging complete"
echo "=========================================="

echo
echo "Token Analysis:"
echo "- Check if the token is expired (exp claim)"
echo "- Verify the issuer (iss) matches your Keycloak configuration"
echo "- Ensure your Spring Security is configured to accept this issuer"
echo "- Check if the user has ADMIN role in the token"

echo
echo "Common Issues:"
echo "1. JWT issuer mismatch between Keycloak and Spring Security config"
echo "2. Token expired"
echo "3. Missing ADMIN role in the token"
echo "4. Spring Security not configured for JWT validation"
echo "5. CORS configuration blocking requests"

echo
echo "Next Steps:"
echo "1. Check your application.yml/properties for JWT configuration"
echo "2. Look at Spring Security configuration"
echo "3. Check if Keycloak is running and accessible"
echo "4. Verify the token contains required roles/claims"



curl -X POST \
     http://localhost:8080/realms/stockfellow/protocol/openid-connect/token \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -d 'grant_type=password' \
     -d 'client_id=public-client' \
     -d 'username=adb@mail.com' \
     -d 'password=1234'