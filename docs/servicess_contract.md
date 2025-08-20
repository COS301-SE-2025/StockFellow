# StockFellow Service Contracts 

### Service Contracts Overview

The StockFellow system is composed of several microservices, each exposing RESTful APIs over HTTP. All services use JSON as the primary data format for requests and responses. Communication protocols are exclusively REST (HTTP/1.1), with no evidence of gRPC or other protocols in the provided code. Services are accessed via a central API Gateway (ProxyController), which handles routing, authentication forwarding (via JWT Bearer tokens), and header propagation (e.g., X-User-Id, X-User-Name). This enforces loose coupling, as clients interact only with the gateway, and services can evolve independently.

**Key Principles:**
- **Versioning:** Each service exposes a version in its root GET endpoint (e.g., "2.1.0" for User Service, "2.0.0" for Group Service, "1.0.0" for Notification Service). API paths are not versioned (e.g., no /v1/), but breaking changes would require path updates or header-based versioning.
- **Data Formats:** JSON for all payloads. Multipart/form-data is used for file uploads (e.g., PDF in User Service).
- **Authentication:** JWT Bearer tokens are required for most endpoints (forwarded via Authorization header). The gateway extracts claims (e.g., sub as X-User-Id) and forwards them.
- **Error Handling:** Standard HTTP status codes (e.g., 400 for bad requests, 401 for unauthorized, 500 for internal errors). Responses include a JSON body with { "error": "message" } or more detailed maps.
- **Timeouts and Retries:** Not explicitly defined in code; rely on default HTTP client timeouts. Services should implement idempotency for retries.
- **Testability:** Endpoints are testable via tools like Postman or Swagger. Integration tests can mock the gateway.
- **Inter-Service Communication:** Services do not directly call each other; interactions are client-orchestrated via the gateway.
- **Loose Coupling:** Gateway routing allows services to be deployed/scaled independently.

---

## 1. User Service (/api/users)

- **Description:** Handles user profiles, verification, affordability analysis, and admin stats.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/users (proxied via gateway).
- **Version:** 2.1.0 (updated from 2.0.0).
- **Authentication:** Required for most endpoints (JWT via gateway headers: X-User-Id, X-User-Name, X-User-Roles).
- **Error Handling:** 4xx/5xx with JSON { "error": "...", "message": "..." }.

### Endpoints:

| Method | Path | Description | Request Params/Body | Response (200 OK) | Error Examples |
|--------|------|-------------|---------------------|-------------------|----------------|
| GET | / | Get service info | None | JSON: { "service": "User Service", "version": "2.1.0", "database": "PostgreSQL", "endpoints": [list] } | 500: { "error": "Internal server error" } |
| POST | /register | Register new user | JSON: User registration data | JSON: User object with registration confirmation | 400: Validation errors, 409: User already exists |
| GET | /profile | Get authenticated user's profile | Headers: X-User-Id (required) | JSON: User object with profile details | 401: { "error": "User not authenticated" }, 404: { "error": "User not found" } |
| POST | /verifyID | Verify user ID via PDF upload | Form: file (MultipartFile, PDF required), userId (optional string); Headers: X-User-Id (fallback) | JSON: { "success": true, "message": "...", "idNumber": "...", "extractedInfo": { "dateOfBirth": "...", "gender": "...", "citizenship": "..." }, "documentId": "...", "verificationTimestamp": long, "user": { ... } } | 400: { "error": "Invalid file", "message": "..." }, 409: { "error": "Already verified" }, 404: User not found |
| POST | /affordability/analyze | Analyze user affordability | JSON: Financial data for analysis | JSON: { "tier": int, "analysis": object, "recommendations": [string[]] } | 400: Invalid data, 401: Unauthorized |
| GET | /{id} | Get user by ID | Path: id (string); Headers: X-User-Id, X-User-Roles | JSON: User object | 401: Unauthorized, 403: { "error": "Access denied" }, 404: Not found |
| GET | /{id}/affordability | Get user affordability tier | Path: id (string); Headers: X-User-Id | JSON: { "userId": string, "tier": int, "lastUpdated": timestamp } | 401: Unauthorized, 404: Not found |
| GET | /search | Search users by name (admin only) | Query: name (string); Headers: X-User-Roles (must include "admin") | JSON: { "users": [User[]], "count": int } | 403: { "error": "Admin access required" } |
| GET | /verified | Get verified users (admin only) | Headers: X-User-Roles ("admin") | JSON: { "verifiedUsers": [User[]], "count": int } | 403: Admin required |
| GET | /stats | Get user stats (admin only) | Headers: X-User-Roles ("admin") | JSON: { "totalUsers": int, "verifiedUsers": int, "unverifiedUsers": int, "incompleteProfiles": int, "verificationRate": int } | 403: Admin required |
| GET | /affordability/stats | Get affordability stats (admin only) | Headers: X-User-Roles ("admin") | JSON: { "tierDistribution": object, "averageTier": double, "totalAnalyzed": int } | 403: Admin required |

### Data Schemas:
- **User:** { "id": string, "userId": string, "username": string, "email": string, "idVerified": boolean, "affordabilityTier": int, "updatedAt": timestamp, ... }
- **AffordabilityAnalysis:** { "tier": int, "income": double, "expenses": double, "creditScore": int, "riskProfile": string, "recommendations": [string[]] }

### Integration Notes:
- Used by Group Service for member validation and tier-based group assignments
- Affordability analysis integrated with group creation workflow
- File uploads use multipart/form-data for ID verification

---

## 2. Group Service (/api/groups)

- **Description:** Manages groups, members, payouts, searches, and tier-based stokvel creation.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/groups (proxied via gateway).
- **Version:** 2.0.0.
- **Authentication:** Required (JWT via headers).
- **Error Handling:** 4xx/5xx with JSON { "error": "..." }.

### Endpoints:

| Method | Path | Description | Request Params/Body | Response (200 OK) | Error Examples |
|--------|------|-------------|---------------------|-------------------|----------------|
| GET | / | Get service info | None | JSON: { "service": "Group Service", "version": "2.0.0", "endpoints": [detailed list] } | N/A |
| POST | /create | Create a new group | JSON: { "name": string (req), "minContribution": double (req), "maxMembers": int (req), "visibility": string (req), "contributionFrequency": string (req), "payoutFrequency": string (req), "description": string, "profileImage": string, "contributionDate": ISO date, "payoutDate": ISO date, "members": [string[]] } | 201: { "message": "Group created successfully", "groupId": string, "eventId": string } | 400: { "error": "name is required" }, 401: Unauthorized |
| PUT | /{groupId} | Update group details (admin only) | Path: groupId; JSON: UpdateGroupRequest; Headers: X-User-Id | JSON: Updated Group object | 400: Invalid data, 401: Unauthorized, 403: Not admin, 404: Not found |
| GET | /user | Get user's groups | Headers: X-User-Id | JSON: [Group[]] (list of user's groups) | 401: Unauthorized, 500: Internal error |
| GET | /search | Search public groups | Query: query (string, optional) | JSON: { "groups": [Group summaries], "totalCount": int, "query": string } | 500: { "error": "Internal server error during search" } |
| GET | /{groupId}/view | View group details/events | Path: groupId; Headers: X-User-Id | JSON: { "group": Group, "events": [Event[]], "userPermissions": { "isMember": bool, "isAdmin": bool, "canViewRequests": bool } } | 401: Unauthorized, 403: { "error": "Access denied..." }, 404: Not found |
| GET | /{groupId}/join | Request to join a group | Path: groupId; Headers: X-User-Id, X-Username | JSON: { "message": "Join request sent successfully", "groupId": string, "eventId": string, "status": "pending" } | 400: Invalid request, 401: Unauthorized, 403: Cannot join private, 404: Not found |
| GET | /{groupId}/requests | Get join requests (admin only) | Path: groupId; Headers: X-User-Id | JSON: { "groupId": string, "groupName": string, "requests": [JoinRequest[]], "totalPendingRequests": int } | 401: Unauthorized, 403: Access denied, 404: Not found |
| POST | /{groupId}/request | Process join request (admin only) | Path: groupId; JSON: { "requestId": string (req), "action": string (req, "accept"/"reject") }; Headers: X-User-Id | JSON: { "message": "Join request [action]ed successfully", "groupId": string, "requestId": string, "action": string, "eventId": string, "result": string } | 400: Invalid data, 401: Unauthorized, 403: Not admin, 404: Not found |
| GET | /{groupId}/next-payee | Get next payout recipient | Path: groupId | JSON: { "groupId": string, "groupName": string, "recipientId": string, "recipientUsername": string, "recipientRole": string, "currentPosition": int, "totalMembers": int, "groupBalance": double, "lastPayoutRecipient": string, "lastPayoutDate": date, "payoutFrequency": string, "nextPayoutDate": date } | 400: Invalid request, 404: Group not found |
| POST | /{groupId}/record-payout | Record payout and advance | Path: groupId; JSON: { "recipientId": string (req), "amount": double (req) }; Headers: X-User-Id | JSON: { "message": "Payout recorded successfully", "processedRecipient": string, "processedAmount": double, "nextPayee": { "recipientId": string, "recipientUsername": string, "position": int } } | 400: Invalid data, 403: Access denied, 404: Not found |
| POST | /join-tier | Join/create stokvel by tier | Query: tier (integer, 1-6); Headers: X-User-Id, X-Username | JSON: { "message": string, "groupId": string } | 400: { "error": "Invalid tier (must be 1-6)" }, 401: Unauthorized, 500: Internal error |

### Data Schemas:
- **Group:** { "id": string, "groupId": string, "name": string, "description": string, "profileImage": string, "visibility": string, "minContribution": double, "maxMembers": int, "contributionFrequency": string, "payoutFrequency": string, "members": [Member objects], "balance": double, "createdAt": timestamp }
- **Member:** { "userId": string, "username": string, "role": string, "joinedAt": timestamp }
- **JoinRequest:** { "requestId": string, "userId": string, "username": string, "requestedAt": timestamp, "status": string }
- **Event:** Basic event list for group activities

### Integration Notes:
- Integrates with User Service for member validation and affordability tiers
- Integrates with Transaction Service for payout recording
- Integrates with Notification Service for group events and invites
- Tier-based stokvel creation links to User Service affordability analysis

---

## 3. Transaction Service (/api/transactions)

- **Description:** Manages financial transactions, mandates, and cycles with enhanced querying capabilities.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/transactions (proxied via gateway).
- **Version:** Implicit 1.0.0.
- **Authentication:** Implicit via gateway.
- **Error Handling:** 4xx/5xx with JSON bodies (e.g., 404: empty body).

### Endpoints:

| Method | Path | Description | Request Params/Body | Response (200 OK) | Error Examples |
|--------|------|-------------|---------------------|-------------------|----------------|
| GET | / | Get all transactions | None | JSON: [Transaction[]] (list of all transactions) | N/A (always 200 if no errors) |
| GET | /{transactionId} | Get transaction by ID | Path: transactionId (UUID) | JSON: Transaction object | 404: Empty body |
| GET | /cycle/{cycleId} | Get transactions by cycle ID (newest first) | Path: cycleId (UUID) | JSON: [Transaction[]] ordered by createdAt desc | N/A |
| GET | /payer/{payerUserId} | Get transactions by payer (newest first) | Path: payerUserId (UUID) | JSON: [Transaction[]] ordered by createdAt desc | N/A |
| GET | /status/{status} | Get transactions by status | Path: status (string: PENDING/COMPLETED/FAILED/CANCELLED) | JSON: [Transaction[]] | N/A |

### Data Schemas:
- **Transaction:** { "id": UUID, "cycleId": UUID, "payerUserId": UUID, "amount": double, "status": string (PENDING/COMPLETED/FAILED/CANCELLED), "transactionType": string (DEBIT/CREDIT), "description": string, "createdAt": timestamp, "updatedAt": timestamp }

### Integration Notes:
- Interacts with Group Service for cycle-based transactions
- Enhanced querying for payout tracking and financial reporting
- Supports financial audit trails with comprehensive filtering

---

## 4. Notification Service (/api/notifications)

- **Description:** Handles sending and managing notifications with bulk capabilities.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/notifications (proxied via gateway).
- **Version:** 1.0.0.
- **Authentication:** Required (JWT via X-User-Id, X-User-Name headers).
- **Error Handling:** 4xx/5xx with JSON { "error": "..." }.

### Endpoints:

| Method | Path | Description | Request Params/Body | Response (200 OK) | Error Examples |
|--------|------|-------------|---------------------|-------------------|----------------|
| GET | / | Get service info | None | JSON: { "service": "Notification Service", "version": "1.0.0", "endpoints": [list] } | N/A |
| POST | /send | Send a notification | JSON: { "userId": string (req), "groupId": string, "type": string (req, e.g., "GROUP_INVITE"), "title": string (req), "message": string (req), "channel": string (req, e.g., "EMAIL"), "priority": string (default "NORMAL"), "metadata": object } | 201: { "message": "Notification sent successfully", "notificationId": string } | 400: { "error": "User ID is required" }, 500: { "error": "Failed to send notification: ..." } |
| GET | /user | Get user's notifications | Headers: X-User-Id (req) | JSON: { "notifications": [Notification[]], "count": int } | 401: { "error": "User ID not found in request" } |
| GET | /user/unread | Get unread notifications | Headers: X-User-Id | JSON: { "notifications": [Notification[]], "count": int } | 401: Unauthorized |
| PUT | /{notificationId}/read | Mark as read | Path: notificationId (string); Headers: X-User-Id | JSON: { "message": "Notification marked as read", "notificationId": string } | 400: Failed to mark, 401: Unauthorized |
| GET | /user/count | Get unread count | Headers: X-User-Id | JSON: { "unreadCount": long, "userId": string } | 401: Unauthorized |
| PUT | /user/read-all | Mark all as read | Headers: X-User-Id | JSON: { "message": "All notifications marked as read", "markedCount": int } | 401: Unauthorized |
| POST | /bulk | Send bulk notifications (admin) | JSON: { "userIds": [string[]] (req), "type": string (req), "title": string (req), "message": string (req), "channel": string (req), "priority": string, "groupId": string, "metadata": object }; Headers: X-User-Id (admin) | JSON: { "message": "Bulk notifications processed", "totalRequested": int, "successCount": int, "failureCount": int, "notificationIds": [string[]] } | 400: { "error": "User IDs list is required" }, 401: Unauthorized |
| GET | /{notificationId} | Get notification by ID | Path: notificationId; Headers: X-User-Id | JSON: Notification object | 404: { "error": "Notification not found" }, 401: Unauthorized |

### Data Schemas:
- **Notification:** { "notificationId": string, "userId": string, "groupId": string, "type": string, "title": string, "message": string, "channel": string, "priority": string, "metadata": object, "read": boolean, "createdAt": timestamp }

### Valid Values:
- **Channel:** EMAIL, SMS, PUSH, IN_APP
- **Priority:** LOW, NORMAL, HIGH, URGENT
- **Type:** GROUP_INVITE, GROUP_JOIN, PAYMENT_DUE, PAYMENT_RECEIVED, PAYOUT_READY, SYSTEM_UPDATE, REMINDER, WELCOME

### Integration Notes:
- Triggered by Group Service for group-related events
- Supports bulk notifications for admin operations
- Multi-channel delivery system

---

## 5. MFA Service (/api/mfa)

- **Description:** Handles multi-factor authentication via email OTP.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/mfa (proxied via gateway).
- **Version:** Implicit 1.0.0.
- **Authentication:** Varies per endpoint.
- **Error Handling:** Standard HTTP with JSON responses.

### Endpoints:

| Method | Path | Description | Request Params/Body | Response (200 OK) | Error Examples |
|--------|------|-------------|---------------------|-------------------|----------------|
| POST | /send-otp | Send OTP to user email | JSON: { "email": string (req), "userId": string (req) } | JSON: { "success": true, "message": "OTP sent successfully" } | 500: { "success": false, "message": "Failed to send OTP" } |
| POST | /verify-otp | Verify OTP code | JSON: { "email": string (req), "otpCode": string (req) } | JSON: { "success": true, "message": "OTP verified successfully", "sessionToken": string } | 400: { "success": false, "message": "Invalid or expired OTP" }, 500: { "success": false, "message": "OTP verification failed" } |
| GET | /status/{email} | Check if valid OTP exists | Path: email (string) | JSON: { "success": boolean, "message": string } | N/A |
| DELETE | /invalidate/{email} | Invalidate user's OTP | Path: email (string) | JSON: { "success": true, "message": "OTP invalidated" } | N/A |

### Data Schemas:
- **MfaRequest:** { "email": string, "userId": string }
- **MfaVerifyRequest:** { "email": string, "otpCode": string }
- **MfaResponse:** { "success": boolean, "message": string, "sessionToken": string (optional) }

### Integration Notes:
- Used by API Gateway for login flow
- Email-based OTP delivery
- Session token generation for verified users
- OTP invalidation for security (failed attempts, logout)

---

## API Gateway (Proxy for All Services)

- **Description:** Routes requests to services, forwards auth headers, provides health checks.
- **Protocol:** REST (HTTP).
- **Base Path:** /api/**
- **Version:** N/A (transparent proxy).
- **Authentication:** Handles JWT extraction and forwarding.

### Special Endpoints:
- **GET /health:** Health check (no auth required)
- **GET /routes:** Get route configuration (admin only)
- **Generic proxy:** GET/POST/PUT/DELETE/PATCH on /** routes to appropriate services

### Error Handling:
- **404:** No matching route found
- **500:** Proxy failures with { "error": "Gateway error", "message": "..." }

### Integration Notes:
- All client requests flow through gateway
- Enables service versioning, rate limiting, and monitoring
- JWT token extraction and header forwarding to downstream services
- Configurable routing via RouteConfig

---

