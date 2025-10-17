# Swagger API Documentation for StockFellow Services

## User Service API Documentation

```yaml
openapi: 3.0.3
info:
  title: User Service API
  description: API for managing users in the StockFellow platform
  version: 1.0.0
  contact:
    name: StockFellow Team
servers:
  - url: http://localhost:8080
    description: Development server

paths:
  /api/users:
    get:
      summary: Get service information
      description: Returns basic information about the User Service
      tags:
        - Service Info
      responses:
        '200':
          description: Service information
          content:
            application/json:
              schema:
                type: object
                properties:
                  service:
                    type: string
                    example: "User Service"
                  version:
                    type: string
                    example: "1.0.0"
                  endpoints:
                    type: array
                    items:
                      type: string
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/users/profile:
    get:
      summary: Get user profile
      description: Get the profile of the authenticated user
      tags:
        - Users
      security:
        - bearerAuth: []
      responses:
        '200':
          description: User profile
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/users/{id}:
    get:
      summary: Get user by ID
      description: Get a specific user by their ID
      tags:
        - Users
      security:
        - bearerAuth: []
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: User ID
      responses:
        '200':
          description: User details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/users/register:
    post:
      summary: Register new user
      description: Register a new user with required information
      tags:
        - Users
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterUserRequest'
      responses:
        '201':
          description: User registered successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "User registered successfully"
                  userId:
                    type: string
                  eventId:
                    type: string
        '400':
          description: Bad request - Missing required fields
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /auth/login:
    get:
      summary: User login
      description: Login user and return user information
      tags:
        - Authentication
      responses:
        '200':
          description: Login successful
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Login successful"
                  user:
                    type: object
                    properties:
                      userId:
                        type: string
                      email:
                        type: string
                      name:
                        type: string
                      saId:
                        type: string
                      mobileNumber:
                        type: string
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /auth/logout:
    get:
      summary: User logout
      description: Logout user and invalidate session
      tags:
        - Authentication
      responses:
        '200':
          description: Logout successful
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Logged out successfully"

  /sync:
    post:
      summary: Sync user from Keycloak
      description: Sync user data from Keycloak authentication service
      tags:
        - Sync
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserSyncRequest'
      responses:
        '200':
          description: User sync successful
          content:
            application/json:
              schema:
                type: object
                properties:
                  success:
                    type: boolean
                  message:
                    type: string
                  userId:
                    type: string
                  eventId:
                    type: string
                  eventType:
                    type: string
        '400':
          description: Missing required fields
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Sync failed
          content:
            application/json:
              schema:
                type: object
                properties:
                  success:
                    type: boolean
                    example: false
                  error:
                    type: string
                  details:
                    type: string

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    User:
      type: object
      properties:
        userId:
          type: string
        username:
          type: string
        email:
          type: string
        firstName:
          type: string
        lastName:
          type: string
        emailVerified:
          type: boolean
        contactNumber:
          type: string
        idNumber:
          type: string
        createdAt:
          type: string
          format: date-time
        updatedAt:
          type: string
          format: date-time

    RegisterUserRequest:
      type: object
      required:
        - name
        - email
        - saId
        - mobileNumber
      properties:
        name:
          type: string
        email:
          type: string
          format: email
        saId:
          type: string
        mobileNumber:
          type: string

    UserSyncRequest:
      type: object
      required:
        - keycloakId
        - username
        - email
        - firstName
        - lastName
        - idNumber
      properties:
        keycloakId:
          type: string
        username:
          type: string
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        emailVerified:
          type: boolean
        phoneNumber:
          type: string
        idNumber:
          type: string

    ErrorResponse:
      type: object
      properties:
        error:
          type: string
```

---

## Group Service API Documentation

```yaml
openapi: 3.0.3
info:
  title: Group Service API
  description: API for managing investment groups in the StockFellow platform
  version: 1.0.0
  contact:
    name: StockFellow Team
servers:
  - url: http://localhost:8081
    description: Development server

paths:
  /api/groups:
    get:
      summary: Get service information
      description: Returns basic information about the Group Service
      tags:
        - Service Info
      responses:
        '200':
          description: Service information
          content:
            application/json:
              schema:
                type: object
                properties:
                  service:
                    type: string
                    example: "Group Service"
                  version:
                    type: string
                    example: "1.0.0"
                  endpoints:
                    type: array
                    items:
                      type: string

  /api/groups/create:
    post:
      summary: Create a new group
      description: Create a new investment group with specified parameters
      tags:
        - Groups
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateGroupRequest'
      responses:
        '201':
          description: Group created successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Group created successfully"
                  groupId:
                    type: string
                  eventId:
                    type: string
        '400':
          description: Bad request - Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/user:
    get:
      summary: Get user's groups
      description: Get all groups that the authenticated user is a member of
      tags:
        - Groups
      security:
        - bearerAuth: []
      responses:
        '200':
          description: List of user's groups
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Group'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              
  /api/groups/{groupId}:
    get:
      summary: Get details of a specific group
      description: |
        Returns details of a specific group. The response fields vary based on the requesting user's role in the group.
        - Admin/Founder: Full group details including balance, all member contributions, and join requests (Group Activities are handled by Notification service)
        - Member: Group details but hides other members' contribution amounts and requests
        - Non-member: Basic group details (hides balance, all contribution amounts, and requests)
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          description: ID of the group to retrieve
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Group details retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  id:
                    type: string
                    description: MongoDB document ID
                  groupId:
                    type: string
                    description: Business identifier for the group
                  name:
                    type: string
                  adminId:
                    type: string
                  minContribution:
                    type: number
                    format: double
                  balance:
                    type: number
                    format: double
                    description: Only visible to group members
                  maxMembers:
                    type: integer
                  description:
                    type: string
                  profileImage:
                    type: string
                  visibility:
                    type: string
                  contributionFrequency:
                    type: string
                  payoutFrequency:
                    type: string
                  contributionDate:
                    type: string
                    format: date-time
                  payoutDate:
                    type: string
                    format: date-time
                  createdAt:
                    type: string
                    format: date-time
                  members:
                    type: array
                    items:
                      type: object
                      properties:
                        userId:
                          type: string
                        role:
                          type: string
                          enum: [founder, admin, member]
                        joinedAt:
                          type: string
                          format: date-time
                        lastActive:
                          type: string
                          format: date-time
                        contribution:
                          type: number
                          format: double
                          description: Only visible to admins/founders for other members
                  activities:
                    type: array
                    items:
                      type: object
                      properties:
                        type:
                          type: string
                          enum: [contribution, payout, missed_contribution]
                        userId:
                          type: string
                        amount:
                          type: number
                          format: double
                        timestamp:
                          type: string
                          format: date-time
                  requests:
                    type: array
                    description: Only visible to admins and founders
                    items:
                      type: object
                      properties:
                        requestId:
                          type: string
                        userId:
                          type: string
                        status:
                          type: string
                          enum: [accepted, rejected, pending]
                        timestamp:
                          type: string
                          format: date-time
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User doesn't have permission to view this group
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/join:
    post:
      summary: Request to join a group
      description: |
        Creates and sends a join request to the group with status "pending".
        The request must be approved by a group admin/founder.
      tags:
        - Group Requests
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
          description: Group ID to join
      responses:
        '201':
          description: Join request sent
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Join request sent"
                  requestId:
                    type: string
                  groupId:
                    type: string
                  userId:
                    type: string
                  status:
                    type: string
                    enum: [pending, accepted, rejected]
                    default: pending
                  timestamp:
                    type: string
                    format: date-time
        '400':
          description: |
            Bad request - Possible reasons:
            - User already has a pending request
            - User is already a member
            - Group is full
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - Group is private and doesn't accept requests
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  
  /api/groups/{groupId}/leave:
    delete:
      summary: Leave a group
      description: |
        Removes the authenticated user from the group members list.
        Admins/founders cannot leave unless they transfer ownership first.
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
          description: Group ID to leave
      responses:
        '200':
          description: Successfully left the group
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Successfully left the group"
                  groupId:
                    type: string
        '400':
          description: |
            Bad request - Possible reasons:
            - User is not a member
            - User is the last admin/founder (must transfer ownership first)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  
  /api/groups/{groupId}/requests:
    get:
      summary: Get all pending join requests for a group, where status is "pending"
      description: |
        Returns all pending join requests for a specific group.
        Only accessible to group admins and founders.
      tags:
        - Group Requests
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          description: ID of the group
          required: true
          schema:
            type: string
      responses:
        '200':
          description: List of pending join requests
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/JoinRequest'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not an admin/founder of the group
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/requests/{requestId}/accept:
    post:
      summary: Accept a join request
      description: |
        Accepts a join request, adding the user to the group members with:
        - Role: "member"
        - Contribution: group's minContribution
        Updates the request status to "accepted"
      tags:
        - Group Requests
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          description: ID of the group
          required: true
          schema:
            type: string
        - name: requestId
          in: path
          description: ID of the request to accept
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Request accepted and user added to group
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Join request accepted and user added to group"
                  group:
                    $ref: '#/components/schemas/Group'
        '400':
          description: Bad request - User is already a member
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not an admin/founder of the group
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group or request not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/requests/{requestId}/reject:
    post:
      summary: Reject a join request
      description: |
        Rejects a join request without adding the user to the group.
        Updates the request status to "rejected"
      tags:
        - Group Requests
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          description: ID of the group
          required: true
          schema:
            type: string
        - name: requestId
          in: path
          description: ID of the request to reject
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Request rejected successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Join request rejected"
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not an admin/founder of the group
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group or request not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              
  /api/groups/{groupId}/transfer-ownership:
    post:
      summary: Transfer group ownership
      description: |
        Transfers group ownership to another member.
        Only accessible to current group founder/admin.
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                newOwnerId:
                  type: string
                  description: ID of the member to become new owner
      responses:
        '200':
          description: Ownership transferred successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Ownership transferred successfully"
                  newAdminId:
                    type: string
        '400':
          description: |
            Bad request - Possible reasons:
            - New owner is not a group member
            - New owner is already an admin
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not current owner
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group or new owner not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}:
    patch:
      summary: Update group information
      description: |
        Updates group information. Only accessible to group admins/founders.
        Cannot update members' information through this endpoint.
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                name:
                  type: string
                minContribution:
                  type: number
                  format: double
                maxMembers:
                  type: integer
                description:
                  type: string
                profileImage:
                  type: string
                visibility:
                  type: string
                  enum: [Public, Private]
                contributionFrequency:
                  type: string
                payoutFrequency:
                  type: string
      responses:
        '200':
          description: Group updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Group'
        '400':
          description: |
            Bad request - Possible reasons:
            - Invalid field values
            - Attempt to update restricted fields
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not admin/founder
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/members/{memberId}/role:
    patch:
      summary: Update member role
      description: |
        Updates a member's role. Only accessible to group founder/admin.
        Founder/Admin cannot change their own role.
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
        - name: memberId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                role:
                  type: string
                  enum: [admin, member]
      responses:
        '200':
          description: Role updated successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Member role updated successfully"
                  memberId:
                    type: string
                  newRole:
                    type: string
        '400':
          description: |
            Bad request - Possible reasons:
            - Attempt to change founder role
            - Invalid role value
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: Forbidden - User is not founder
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/contribution:
    patch:
      summary: Update member contribution
      description: |
        Updates the authenticated member's contribution amount.
        New amount must be ≥ group's minContribution.
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                amount:
                  type: number
                  format: double
                  minimum: 0
                  description: Must be ≥ group's minContribution
      responses:
        '200':
          description: Contribution updated successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Contribution updated successfully"
                  newAmount:
                    type: number
                    format: double
        '400':
          description: |
            Bad request - Possible reasons:
            - Amount < minContribution
            - User is not a member
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/groups/{groupId}/members/{memberId}:
    delete:
      summary: Remove a member from the group
      description: |
        Removes a member from the group with role-based permissions:
        - Founders can remove admins and members
        - Admins can only remove members
        - Founders cannot be removed (must transfer ownership first)
        - Members cannot remove anyone
      tags:
        - Groups
      security:
        - bearerAuth: []
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
        - name: memberId
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Member removed successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
                    example: "Member removed successfully"
                  removedMemberId:
                    type: string
        '400':
          description: |
            Bad request - Possible reasons:
            - Attempt to remove founder
            - Attempt to remove yourself
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          description: Unauthorized - Authentication required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '403':
          description: |
            Forbidden - Possible reasons:
            - User lacks permission to remove this member
            - Admin trying to remove another admin
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '404':
          description: Group or member not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    CreateGroupRequest:
      type: object
      required:
        - name
        - minContribution
        - maxMembers
        - visibility
        - contributionFrequency
        - payoutFrequency
      properties:
        name:
          type: string
          description: Group name
        minContribution:
          type: number
          format: double
          minimum: 0.01
          description: Minimum contribution amount
        maxMembers:
          type: integer
          minimum: 1
          description: Maximum number of members
        description:
          type: string
          description: Group description
        profileImage:
          type: string
          description: Profile image URL
        visibility:
          type: string
          enum: [Private, Public]
          description: Group visibility
        contributionFrequency:
          type: string
          enum: [Monthly, Bi-weekly, Weekly]
          description: How often members contribute
        contributionDate:
          type: string
          format: date-time
          description: Date for contributions (ISO format)
        payoutFrequency:
          type: string
          enum: [Monthly, Bi-weekly, Weekly]
          description: How often payouts occur
        payoutDate:
          type: string
          format: date-time
          description: Date for payouts (ISO format)
        members:
          type: array
          items:
            type: string
          description: Initial member list (user IDs)

    Group:
      type: object
      properties:
        groupId:
          type: string
        name:
          type: string
        adminId:
          type: string
        minContribution:
          type: number
          format: double
        maxMembers:
          type: integer
        description:
          type: string
        profileImage:
          type: string
        visibility:
          type: string
          enum: [Private, Public]
        contributionFrequency:
          type: string
          enum: [Monthly, Bi-weekly, Weekly]
        contributionDate:
          type: string
          format: date-time
        payoutFrequency:
          type: string
          enum: [Monthly, Bi-weekly, Weekly]
        payoutDate:
          type: string
          format: date-time
        members:
          type: array
          items:
            $ref: '#/components/schemas/GroupMember'
        createdAt:
          type: string
          format: date-time
        updatedAt:
          type: string
          format: date-time

    GroupMember:
      type: object
      properties:
        userId:
          type: string
        joinedAt:
          type: string
          format: date-time
    
    JoinRequest:
      type: object
      properties:
        requestId:
          type: string
        userId:
          type: string
        profileName:
          type: string
        profileImage:
          type: string
          nullable: true
        timestamp:
          type: string
          format: date-time
        status:
          type: string
          enum: [pending, accepted, rejected]
          default: pending

    ErrorResponse:
      type: object
      properties:
        error:
          type: string
```

---

## Transaction Service API Documentation

```yaml
openapi: 3.0.3
info:
  title: Transaction Service API
  description: API for managing financial transactions in the StockFellow platform
  version: 1.0.0
  contact:
    name: StockFellow Team

servers:
  - url: http://localhost:8080
    description: Development server

tags:
  - name: Mandates
    description: Operations related to user mandates for group participation
  - name: Transactions
    description: Operations related to financial transactions
  - name: Group Cycles
    description: Operations related to group investment cycles

paths:
  # Mandate Endpoints
  /api/mandates:
    post:
      tags:
        - Mandates
      summary: Create a new mandate
      description: Creates a mandate between a user and a group, allowing the user to participate in group investment cycles
      operationId: createMandate
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateMandateRequest'
      responses:
        '201':
          description: Mandate created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MandateResponse'
        '400':
          description: Invalid request parameters
        '409':
          description: Business logic conflict (e.g., mandate already exists)
        '500':
          description: Internal server error

    get:
      tags:
        - Mandates
      summary: Get all mandates
      description: Retrieves a list of all mandates in the system
      operationId: getAllMandates
      responses:
        '200':
          description: List of mandates retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MandateResponse'

  /api/mandates/{mandateId}:
    get:
      tags:
        - Mandates
      summary: Get mandate by ID
      description: Retrieves a specific mandate by its unique identifier
      operationId: getMandateById
      parameters:
        - name: mandateId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the mandate
      responses:
        '200':
          description: Mandate retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MandateResponse'
        '404':
          description: Mandate not found

  /api/mandates/{mandateId}/deactivate:
    put:
      tags:
        - Mandates
      summary: Deactivate a mandate
      description: Deactivates an existing mandate, preventing future transactions
      operationId: deactivateMandate
      parameters:
        - name: mandateId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the mandate to deactivate
      responses:
        '200':
          description: Mandate deactivated successfully
        '404':
          description: Mandate not found

  /api/mandates/group/{groupId}:
    get:
      tags:
        - Mandates
      summary: Get mandates by group
      description: Retrieves all mandates associated with a specific group
      operationId: getMandatesByGroup
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
      responses:
        '200':
          description: Group mandates retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MandateResponse'

  /api/mandates/status/{status}:
    get:
      tags:
        - Mandates
      summary: Get mandates by status
      description: Retrieves all mandates with a specific status
      operationId: getMandatesByStatus
      parameters:
        - name: status
          in: path
          required: true
          schema:
            type: string
            enum: [ACTIVE, INACTIVE, PENDING, EXPIRED]
          description: The status of the mandates to retrieve
      responses:
        '200':
          description: Mandates with specified status retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MandateResponse'

  /api/mandates/group/{groupId}/active:
    get:
      tags:
        - Mandates
      summary: Get active mandates for a group
      description: Retrieves all active mandates for a specific group
      operationId: getActiveMandatesForGroup
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
      responses:
        '200':
          description: Active mandates for the group retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MandateResponse'

  # Transaction Endpoints
  /api/transactions:
    get:
      tags:
        - Transactions
      summary: Get all transactions
      description: Retrieves a list of all transactions in the system
      operationId: getAllTransactions
      responses:
        '200':
          description: List of transactions retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Transaction'

  /api/transactions/{transactionId}:
    get:
      tags:
        - Transactions
      summary: Get transaction by ID
      description: Retrieves a specific transaction by its unique identifier
      operationId: getTransaction
      parameters:
        - name: transactionId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the transaction
      responses:
        '200':
          description: Transaction retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Transaction'
        '404':
          description: Transaction not found

  /api/transactions/cycle/{cycleId}:
    get:
      tags:
        - Transactions
      summary: Get transactions by cycle
      description: Retrieves all transactions associated with a specific group cycle
      operationId: getTransactionsByCycle
      parameters:
        - name: cycleId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the cycle
      responses:
        '200':
          description: Cycle transactions retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Transaction'

  /api/transactions/payer/{payerUserId}:
    get:
      tags:
        - Transactions
      summary: Get transactions by payer
      description: Retrieves all transactions made by a specific payer
      operationId: getTransactionsByPayer
      parameters:
        - name: payerUserId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the payer user
      responses:
        '200':
          description: Payer transactions retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Transaction'

  /api/transactions/status/{status}:
    get:
      tags:
        - Transactions
      summary: Get transactions by status
      description: Retrieves all transactions with a specific status
      operationId: getTransactionsByStatus
      parameters:
        - name: status
          in: path
          required: true
          schema:
            type: string
            enum: [PENDING, COMPLETED, FAILED, CANCELLED]
          description: The status of the transactions to retrieve
      responses:
        '200':
          description: Transactions with specified status retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Transaction'

  # Group Cycle Endpoints
  /api/cycles:
    post:
      tags:
        - Group Cycles
      summary: Create a new group cycle
      description: Creates a new investment cycle for a group
      operationId: createGroupCycle
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateCycleRequest'
      responses:
        '201':
          description: Group cycle created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CycleResponse'
        '400':
          description: Invalid request parameters
        '409':
          description: Business logic conflict (e.g., cycle already exists for the period)
        '500':
          description: Internal server error

    get:
      tags:
        - Group Cycles
      summary: Get all cycles
      description: Retrieves a list of all group cycles in the system
      operationId: getAllCycles
      responses:
        '200':
          description: List of cycles retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CycleResponse'

  /api/cycles/{cycleId}:
    get:
      tags:
        - Group Cycles
      summary: Get cycle by ID
      description: Retrieves a specific group cycle by its unique identifier
      operationId: getCycle
      parameters:
        - name: cycleId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the cycle
      responses:
        '200':
          description: Cycle retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CycleResponse'
        '404':
          description: Cycle not found

  /api/cycles/group/{groupId}:
    get:
      tags:
        - Group Cycles
      summary: Get cycles by group
      description: Retrieves all cycles associated with a specific group
      operationId: getCyclesByGroup
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
      responses:
        '200':
          description: Group cycles retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CycleResponse'

  /api/cycles/status/{status}:
    get:
      tags:
        - Group Cycles
      summary: Get cycles by status
      description: Retrieves all cycles with a specific status
      operationId: getCyclesByStatus
      parameters:
        - name: status
          in: path
          required: true
          schema:
            type: string
            enum: [PENDING, ACTIVE, COMPLETED, CANCELLED]
          description: The status of the cycles to retrieve
      responses:
        '200':
          description: Cycles with specified status retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CycleResponse'

  /api/cycles/group/{groupId}/next:
    get:
      tags:
        - Group Cycles
      summary: Get next upcoming cycle for a group
      description: Retrieves the next upcoming cycle for a specific group
      operationId: getNextCycleForGroup
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
        - name: status
          in: query
          required: false
          schema:
            type: string
            default: PENDING
            enum: [PENDING, ACTIVE, COMPLETED, CANCELLED]
          description: The status of the cycle to retrieve
      responses:
        '200':
          description: Next cycle for the group retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CycleResponse'
        '404':
          description: No upcoming cycle found for the group

  /api/cycles/upcoming:
    get:
      tags:
        - Group Cycles
      summary: Get next upcoming cycle
      description: Retrieves the next upcoming cycle regardless of group
      operationId: getNextUpcomingCycle
      parameters:
        - name: status
          in: query
          required: false
          schema:
            type: string
            default: PENDING
            enum: [PENDING, ACTIVE, COMPLETED, CANCELLED]
          description: The status of the cycle to retrieve
      responses:
        '200':
          description: Next upcoming cycle retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CycleResponse'
        '404':
          description: No upcoming cycle found

  /api/cycles/group/{groupId}/month/{cycleMonth}:
    get:
      tags:
        - Group Cycles
      summary: Get cycle by group and month
      description: Retrieves a cycle for a specific group in a specific month
      operationId: getCycleByGroupAndMonth
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
        - name: cycleMonth
          in: path
          required: true
          schema:
            type: string
            pattern: '^\d{4}-\d{2}$'
            example: '2024-01'
          description: The cycle month in YYYY-MM format
      responses:
        '200':
          description: Cycle for the specified group and month retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CycleResponse'
        '404':
          description: No cycle found for the specified group and month

  /api/cycles/group/{groupId}/earliest:
    get:
      tags:
        - Group Cycles
      summary: Get earliest cycles for a group
      description: Retrieves the earliest cycles for a specific group
      operationId: getEarliestCyclesForGroup
      parameters:
        - name: groupId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          description: The unique identifier of the group
      responses:
        '200':
          description: Earliest cycles for the group retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CycleResponse'

components:
  schemas:
    CreateMandateRequest:
      type: object
      required:
        - payerUserId
        - groupId
      properties:
        payerUserId:
          type: string
          format: uuid
          description: The unique identifier of the payer user
          example: 550e8400-e29b-41d4-a716-446655440000
        groupId:
          type: string
          format: uuid
          description: The unique identifier of the group
          example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        amount:
          type: number
          format: double
          description: The mandate amount
          example: 1000.00
        frequency:
          type: string
          enum: [MONTHLY, QUARTERLY, ANNUALLY]
          description: The payment frequency
          example: MONTHLY

    MandateResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
          description: The unique identifier of the mandate
          example: 550e8400-e29b-41d4-a716-446655440000
        payerUserId:
          type: string
          format: uuid
          description: The unique identifier of the payer user
          example: 550e8400-e29b-41d4-a716-446655440000
        groupId:
          type: string
          format: uuid
          description: The unique identifier of the group
          example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        amount:
          type: number
          format: double
          description: The mandate amount
          example: 1000.00
        status:
          type: string
          enum: [ACTIVE, INACTIVE, PENDING, EXPIRED]
          description: The current status of the mandate
          example: ACTIVE
        frequency:
          type: string
          enum: [MONTHLY, QUARTERLY, ANNUALLY]
          description: The payment frequency
          example: MONTHLY
        createdAt:
          type: string
          format: date-time
          description: The timestamp when the mandate was created
          example: '2024-01-15T10:30:00Z'
        updatedAt:
          type: string
          format: date-time
          description: The timestamp when the mandate was last updated
          example: '2024-01-15T10:30:00Z'

    Transaction:
      type: object
      properties:
        id:
          type: string
          format: uuid
          description: The unique identifier of the transaction
          example: 550e8400-e29b-41d4-a716-446655440000
        cycleId:
          type: string
          format: uuid
          description: The unique identifier of the associated cycle
          example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        payerUserId:
          type: string
          format: uuid
          description: The unique identifier of the payer user
          example: 550e8400-e29b-41d4-a716-446655440000
        amount:
          type: number
          format: double
          description: The transaction amount
          example: 1000.00
        status:
          type: string
          enum: [PENDING, COMPLETED, FAILED, CANCELLED]
          description: The current status of the transaction
          example: COMPLETED
        transactionType:
          type: string
          enum: [DEBIT, CREDIT]
          description: The type of transaction
          example: DEBIT
        description:
          type: string
          description: Description of the transaction
          example: Monthly investment contribution
        createdAt:
          type: string
          format: date-time
          description: The timestamp when the transaction was created
          example: '2024-01-15T10:30:00Z'
        updatedAt:
          type: string
          format: date-time
          description: The timestamp when the transaction was last updated
          example: '2024-01-15T10:30:00Z'

    CreateCycleRequest:
      type: object
      required:
        - groupId
        - cycleMonth
        - startDate
        - endDate
      properties:
        groupId:
          type: string
          format: uuid
          description: The unique identifier of the group
          example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        cycleMonth:
          type: string
          pattern: '^\d{4}-\d{2}$'
          description: The cycle month in YYYY-MM format
          example: '2024-01'
        startDate:
          type: string
          format: date
          description: The start date of the cycle
          example: '2024-01-01'
        endDate:
          type: string
          format: date
          description: The end date of the cycle
          example: '2024-01-31'
        targetAmount:
          type: number
          format: double
          description: The target amount for the cycle
          example: 50000.00
        description:
          type: string
          description: Description of the cycle
          example: January investment cycle for Group Alpha

    CycleResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
          description: The unique identifier of the cycle
          example: 550e8400-e29b-41d4-a716-446655440000
        groupId:
          type: string
          format: uuid
          description: The unique identifier of the group
          example: 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        cycleMonth:
          type: string
          pattern: '^\d{4}-\d{2}$'
          description: The cycle month in YYYY-MM format
          example: '2024-01'
        startDate:
          type: string
          format: date
          description: The start date of the cycle
          example: '2024-01-01'
        endDate:
          type: string
          format: date
          description: The end date of the cycle
          example: '2024-01-31'
        status:
          type: string
          enum: [PENDING, ACTIVE, COMPLETED, CANCELLED]
          description: The current status of the cycle
          example: ACTIVE
        targetAmount:
          type: number
          format: double
          description: The target amount for the cycle
          example: 50000.00
        currentAmount:
          type: number
          format: double
          description: The current amount collected in the cycle
          example: 25000.00
        description:
          type: string
          description: Description of the cycle
          example: January investment cycle for Group Alpha
        createdAt:
          type: string
          format: date-time
          description: The timestamp when the cycle was created
          example: '2024-01-15T10:30:00Z'
        updatedAt:
          type: string
          format: date-time
          description: The timestamp when the cycle was last updated
          example: '2024-01-15T10:30:00Z'

  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT token for authentication

security:
  - BearerAuth: []
```

---

## Usage Instructions

### 1. **Swagger UI Integration**

To integrate these specifications with your Spring Boot applications, add the following dependencies to each service:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. **Configuration**

Add this configuration to your `application.yml` or `application.properties`:

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
```

### 3. **Annotations**

Add these annotations to your controllers:

```java
@RestController
@Tag(name = "Groups", description = "Group management operations")
@RequestMapping("/api/groups")
public class GroupsController {
    
    @PostMapping("/create")
    @Operation(summary = "Create a new group", 
               description = "Create a new investment group with specified parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request) {
        // Implementation
    }
}
```

### 4. **Access Points**

Once implemented, you can access the documentation at:

- **User Service**: `http://localhost:8080/swagger-ui.html`
- **Group Service**: `http://localhost:8081/swagger-ui.html`
- **Transaction Service**: `http://localhost:8082/swagger-ui.html`

### 5. **API Testing**

Each Swagger UI provides:
- Interactive API testing
- Request/response examples
- Schema documentation
- Authentication testing
- Export capabilities (JSON, YAML)

This comprehensive documentation covers all endpoints, request/response schemas, authentication requirements, and error handling for all three services in your StockFellow platform.