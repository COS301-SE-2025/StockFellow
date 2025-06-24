# StockFellow API Gateway

The API Gateway serves as the entry point to the system's microservices. It validates all requests sent to it and then performs: load balancing/rate limiting, authentication and routing to the desired microservice. It provides a unified interface to the system for both the Web and Mobile applications.

## Features

* **Unified API Access**: Single entry point for all microservices
* **Authentication and Authorization**: Integrated with Keycloak using Spring Security
* **Request Routing**: Uses the fan-out pattern to aggregate all resources required for the request
* **Rate Limiting**: Acts as gateway to protect backend services from excessive loads
* **Request Validation**: Ensures request data is valid before passing it on to backend services
* **Health Monitoring**: Built-in health checks and metrics via Spring Boot Actuator
* **CORS Support**: Configurable cross-origin resource sharing
* **Logging**: Comprehensive request/response logging similar to Apache Combined Log format

## Technologies

* **Java 11** with **Spring Boot 2.7.x**
* **Spring Security** for authentication and authorization
* **Keycloak Integration** via Spring Security adapters
* **Maven** for dependency management and builds
* **Spring Boot Actuator** for monitoring and health checks
* **Docker** for containerization
* **SLF4J/Logback** for logging

## Project Structure

```
api-gateway/
├── pom.xml                                    # Maven configuration and dependencies
├── Dockerfile                                 # Production Docker build
├── Dockerfile.dev                            # Development Docker build (optional)
└── src/
    └── main/
        ├── java/com/stockfellow/gateway/
        │   ├── GatewayApplication.java        # Main Spring Boot application
        │   ├── controller/
        │   │   ├── ProxyController.java       # Handles request proxying to services
        │   │   └── AuthController.java        # Authentication endpoints
        │   ├── config/
        │   │   ├── RouteConfig.java           # Route definitions and configuration
        │   │   ├── SecurityConfig.java        # Keycloak security configuration
        │   │   ├── RateLimitConfig.java       # Rate limiting setup
        │   │   └── LoggingConfig.java         # Logging configuration
        │   ├── filter/
        │   │   ├── RateLimitFilter.java       # Rate limiting implementation
        │   │   └── LoggingFilter.java         # Request/response logging
        │   └── model/
        │       └── Route.java                 # Route data model
        └── resources/
            ├── application.yml                # Default configuration
            ├── application-docker.yml         # Docker environment config
            └── keycloak.json                  # Keycloak client configuration
```

## Dependencies (Maven)

Key dependencies defined in `pom.xml`:

* **spring-boot-starter-web** - Web framework and embedded Tomcat
* **spring-boot-starter-security** - Security framework
* **keycloak-spring-boot-starter** - Keycloak integration
* **keycloak-admin-client** - Direct Keycloak authentication
* **spring-boot-starter-actuator** - Health checks and metrics

## Keycloak Integration

* Uses **Spring Security Keycloak adapters** with OpenID Connect protocol
* Configured via `keycloak.json` and Spring Security configuration
* Supports both redirect-based authentication and direct token access
* Uses Docker to run - integrated into docker-compose workflow

### Keycloak Development Setup
To run Keycloak standalone (FOR DEV PURPOSES ONLY):
```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```
Then access at http://localhost:8080/admin/

## API Routes

### Public Routes (No Authentication Required)
* **GET/POST/PUT/DELETE** `/api/user/**` → Routes to User Service (port 4000)
* **GET/POST/PUT/DELETE** `/api/group/**` → Routes to Group Service (port 4040)
* **GET/POST/PUT/DELETE** `/api/**` → Default route to User Service

### Protected Routes (Authentication Required)
* **GET/POST/PUT/DELETE** `/api/transaction/**` → Routes to Transaction Service (port 4080)

### Authentication Endpoints
* **GET** `/login` → Redirect to Keycloak login
* **GET** `/register` → Redirect to Keycloak registration  
* **GET** `/logout` → Redirect to Keycloak logout
* **POST** `/auth/login` → Direct username/password authentication

### Monitoring Endpoints
* **GET** `/actuator/health` → Health check status
* **GET** `/actuator/info` → Application information
* **GET** `/actuator/metrics` → Application metrics

## Rate Limiting

Each route has configurable rate limiting:
* **Window**: 15 minutes (900,000ms)
* **Max Requests**: 10 per window per client IP
* **Storage**: In-memory (production should use Redis)

## Configuration

### Environment-Specific Configs
* `application.yml` - Default configuration
* `application-docker.yml` - Docker environment overrides

### Key Configuration Properties
```yaml
server:
  port: 3000

keycloak:
  realm: stockfellow
  auth-server-url: http://keycloak:8080/
  resource: public-client

services:
  user-service:
    url: "http://user-service:4000"
  group-service:
    url: "http://group-service:4040"
  transaction-service:
    url: "http://transaction-service:4080"
```

## How to Build and Run

### Using Docker Compose (Recommended)
```bash
# Start all
docker-compose up --build

# API gateway only 
docker-compose build api-gateway
```

### Local Development
```bash
# Build 
mvn clean install

# Run 
mvn spring-boot:run

# Run the JAR 
java -jar target/api-gateway-1.0.0.jar
```

### Development with Hot Reload
1. Add Spring Boot DevTools dependency to `pom.xml`
2. Use `Dockerfile.dev` for faster rebuilds during development
3. Enable automatic restart on code changes

## Testing the Gateway

### Basic Connectivity
```bash
# Test public endpoint (should work)
curl http://localhost:3000/api/user

# Test protected endpoint (should return 401 without auth)
curl http://localhost:3000/api/transaction

# Test health check
curl http://localhost:3000/actuator/health
```

### Authentication Flow
1. Navigate to `http://localhost:3000/api/transaction` - should redirect to Keycloak login
2. Login with valid credentials - should proxy to transaction service
3. Access `http://localhost:3000/login` - should redirect to Keycloak login page

## Setting up Keycloak (For Development)

Running the `docker-compose up` command automatically imports the `realm-exports/stockfellow-realm.json` file to set up the Keycloak realm.

### Required Realm Configuration
The realm should contain:
* **public-client** - For public authentication flows
* **confidential-client** - For server-to-server communication
* **stockfellow** realm name

### Creating Test Users
1. Access Keycloak admin console: http://localhost:8080/admin/
2. Login with admin/admin
3. Navigate to Users → Add User
4. Set username, email, and password
5. Assign appropriate roles if needed

## Debugging

### Java Application Debugging
* **Debug Port**: 5005 (exposed in Docker)
* **JVM Debug Args**: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`
* **IDE Setup**: Connect remote debugger to `localhost:5005`

### Logging
* **Request/Response**: Automatic logging via LoggingFilter
* **Application Logs**: Available via `docker logs api-gateway`
* **Log Level**: Configurable via application.yml

## Performance Considerations

* **First Build**: 5-10 minutes (downloads dependencies)
* **Subsequent Builds**: 30-60 seconds (cached layers)
* **Memory Usage**: ~256MB-512MB JVM heap
* **Startup Time**: ~30-45 seconds

## Production Deployment

### Security Considerations
* Remove development profiles in production
* Use external Keycloak instance (not start-dev)
* Configure proper SSL/TLS certificates
* Use Redis for distributed rate limiting
* Enable proper CORS policies
* Set up log aggregation

### Scaling
* Stateless design allows horizontal scaling
* Rate limiting state should use Redis for multi-instance deployments
* Health checks enable load balancer integration

## Migration Notes

This API Gateway was migrated from a Node.js/Express implementation to Java/Spring Boot while maintaining:
* ✅ Same REST endpoints and functionality
* ✅ Same Keycloak integration
* ✅ Same rate limiting behavior  
* ✅ Same request routing logic
* ✅ Same Docker deployment model

**Key improvements in Java version:**
* Type safety and compile-time error checking
* Better IDE support and debugging
* Enterprise-grade monitoring and health checks
* More robust error handling and logging
* Better memory management and performance tuning options