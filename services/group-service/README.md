# StockFellow Group Service

This service manages investment groups (stokvels) for the StockFellow fintech platform. It provides REST APIs for group creation, membership management, join requests, contributions, payouts, and group search.

## Stokvel Management rules

### Joining Rules
- When a user is verified with a tier, they are automatically assigned to a stokvel of the same tier
- If there are no available stokvels in that user's tier, a new stokvel is created with that user as its first member
- Else, the system searches for the oldest group which is not full to add the new user to 
### Creation by system rules
- Stokvel tier is set to the first members tier
- Max members is set to 10
- Minimum contribution amount is set to the minimum amount of the tier's range(e.g. If tier 4 is R1000-R1500, Minimum contribution amount = R1000)
- Stokvel name is set to "Stokvel #XXX" on default.
- Contribution/Payout Date is set the date of stokvel creation
- Contribution/Payout Frequency is set to monthly.
### Management Rules
- Group admins can edit, name, members, Contribution/Payout date and frequency
- After a full payout cycle, the tier of the stokvel is evaluated to the average tier of each member


## Features

- Create and manage stokvel groups
- Membership and join request workflows
- Contribution and payout tracking
- Role-based access (admin, member)
- Event sourcing for auditability
- MongoDB persistence
- JWT authentication (Keycloak-ready) via gateway
- OpenAPI/Swagger documentation

## Endpoints

- `POST /api/groups/create` – Create a new group
- `GET /api/groups/user` – Get groups for authenticated user
- `GET /api/groups/{groupId}/view` – View group details and events
- `GET /api/groups/{groupId}/join` – Request to join a group (public groups only)
- `GET /api/groups/{groupId}/requests` – Get all join requests for a group (admin only)
- `POST /api/groups/{groupId}/request` – Process join request (accept/reject)
- `GET /api/groups/search?query=<search_term>` – Search public groups

## Technologies

- Java 17
- Spring Boot
- Spring Data MongoDB
- Spring Security (JWT)
- Keycloak (recommended for production)
- Docker

## Getting Started

### Prerequisites

- Java 17+
- Docker (for MongoDB and Keycloak)
- Maven

### Running Locally

1. **Start MongoDB**  
   `docker run -d -p 27017:27017 mongo:latest`

2. **(Optional) Start Keycloak for Auth**  
   `docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev`

3. **Build & Run Service**  
4. **Access API Docs**  
Visit [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

## Configuration

Edit `src/main/resources/application.properties` for MongoDB and JWT settings.

## Event Sourcing

All group actions are recorded as events for audit and state rebuild. See [`ReadModelService`](src/main/java/com/stockfellow/groupservice/service/ReadModelService.java) for details.

## Security

- JWT authentication is supported (see [`JwtConfig`](src/main/java/com/stockfellow/groupservice/config/JwtConfig.java)).
- For development, security may be relaxed (see [`SecurityConfig`](src/main/java/com/stockfellow/groupservice/config/SecurityConfig.java)).

## Contributing

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes
4. Push to the branch
5. Open a pull request


---

For more details, see the [Swagger API Docs](../../swagger_api_docs.md) and [Architecture Docs](../../docs/architecture/).