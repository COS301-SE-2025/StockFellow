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
  - url: http://localhost:8082
    description: Development server

paths:
  /api/transactions:
    get:
      summary: Get service information
      description: Returns basic information about the Transaction Service
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
                    example: "Transaction Service"
                  version:
                    type: string
                    example: "1.0.0"
                  endpoints:
                    type: array
                    items:
                      type: string

  /api/transactions/users:
    post:
      summary: Create user for transactions
      description: Create a user in the transaction system
      tags:
        - Users
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTransactionUserRequest'
      responses:
        '200':
          description: User created successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  userId:
                    type: string
                  financialTier:
                    type: string
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/transactions/mandates:
    post:
      summary: Create debit order mandate
      description: Create a mandate for debit order transactions
      tags:
        - Mandates
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateMandateRequest'
      responses:
        '200':
          description: Mandate created successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  mandateId:
                    type: string
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/transactions/debit-orders:
    post:
      summary: Process debit order
      description: Process a debit order transaction
      tags:
        - Transactions
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DebitOrderRequest'
      responses:
        '200':
          description: Debit order processed successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  transactionId:
                    type: string
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/transactions/payouts:
    post:
      summary: Process payout
      description: Process a payout transaction
      tags:
        - Transactions
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PayoutRequest'
      responses:
        '200':
          description: Payout processed successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  transactionId:
                    type: string
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /api/transactions/schedules:
    post:
      summary: Schedule transaction
      description: Schedule a recurring transaction
      tags:
        - Scheduling
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ScheduleTransactionRequest'
      responses:
        '200':
          description: Transaction scheduled successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  scheduleId:
                    type: string
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  schemas:
    CreateTransactionUserRequest:
      type: object
      required:
        - userId
        - email
        - phone
        - idNumber
      properties:
        userId:
          type: string
          description: User ID
        email:
          type: string
          format: email
          description: User email
        phone:
          type: string
          description: User phone number
        idNumber:
          type: string
          description: User ID number

    CreateMandateRequest:
      type: object
      required:
        - userId
        - bankAccount
      properties:
        userId:
          type: string
          description: User ID
        bankAccount:
          type: string
          description: Bank account details

    DebitOrderRequest:
      type: object
      required:
        - userId
        - groupId
        - amount
      properties:
        userId:
          type: string
          description: User ID
        groupId:
          type: string
          description: Group ID
        amount:
          type: number
          format: double
          minimum: 0.01
          description: Transaction amount

    PayoutRequest:
      type: object
      required:
        - userId
        - groupId
        - amount
      properties:
        userId:
          type: string
          description: User ID
        groupId:
          type: string
          description: Group ID
        amount:
          type: number
          format: double
          minimum: 0.01
          description: Payout amount

    ScheduleTransactionRequest:
      type: object
      required:
        - userId
        - groupId
        - type
        - amount
        - frequency
        - nextRun
      properties:
        userId:
          type: string
          description: User ID
        groupId:
          type: string
          description: Group ID
        type:
          type: string
          enum: [debit, payout]
          description: Transaction type
        amount:
          type: number
          format: double
          minimum: 0.01
          description: Transaction amount
        frequency:
          type: string
          enum: [daily, weekly, monthly]
          description: Schedule frequency
        nextRun:
          type: string
          format: date
          description: Next execution date (YYYY-MM-DD)

    ErrorResponse:
      type: object
      properties:
        error:
          type: string
          description: Error message
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