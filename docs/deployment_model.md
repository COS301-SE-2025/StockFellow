# StockFellow System Deployment Model

## 1. Executive Summary

The StockFellow system is deployed as a containerized microservices architecture using Docker containers orchestrated through Docker Compose. The system follows a multi-tier architecture pattern with clear separation of concerns across presentation, business logic, and data layers.

## 2. Target Environment

### 2.1 Deployment Environment Options

**Primary Target: Cloud-Native Deployment**
- **Cloud Platforms**: AWS, Azure, Google Cloud Platform
- **Container Orchestration**: Kubernetes (production), Docker Swarm (development/staging)
- **Current Configuration**: Docker Compose (development/local deployment)

**Secondary Target: Hybrid Deployment**
- On-premises infrastructure with cloud backup and disaster recovery
- Edge deployment capabilities for distributed access

**Development Environment**
- Local development using Docker Compose
- CI/CD pipeline integration with container registries

### 2.2 Infrastructure Requirements

**Minimum System Requirements:**
- CPU: 8 vCPUs (16 recommended)
- Memory: 16GB RAM (32GB recommended)
- Storage: 100GB SSD (with volume expansion capability)
- Network: 1Gbps connection with load balancing

## 3. Deployment Topology

### 3.1 Architecture Pattern
The system implements a **containerized microservices architecture** with the following characteristics:


- **API Gateway Pattern**: Centralized entry point for all client requests
- **Database per Service**: Each microservice maintains its own data store
- **Event-Driven Communication**: Asynchronous messaging via ActiveMQ

### 3.2 Service Topology

#### **Presentation Tier**
- **API Gateway** (Port 3000): Central routing and authentication hub
- **Load Balancer**: (Future implementation) Traffic distribution

#### **Business Logic Tier**
- **User Service** (Port 4020): User management and profile operations
- **Group Service** (Port 4040): Group and community management
- **Transaction Service** (Port 4080): Financial transaction processing
- **Notification Service** (Port 4050): Real-time notifications and messaging
- **MFA Service** (Port 8087): Multi-factor authentication

#### **Data Tier**
- **PostgreSQL Clusters**: 
  - Keycloak DB (Port 5432)
  - User Service DB (Port 5431)
  - Notification DB (Port 5440)
- **Redis Cache** (Port 6379): Session management and caching
- **MongoDB**: Document storage for groups and MFA data
- **ActiveMQ** (Port 61616): Message broker for asynchronous communication

#### **Security & Identity Tier**
- **Keycloak** (Port 8080): Identity and access management

## 4. Container Architecture

### 4.1 Service Containers

| Service | Base Image | Purpose | Health Check |
|---------|------------|---------|--------------|
| API Gateway | Java Spring Boot | Request routing, authentication | HTTP /actuator/health |
| User Service | Java Spring Boot | User management | HTTP /actuator/health |
| Group Service | Java Spring Boot | Group operations | HTTP /actuator/health |
| Transaction Service | Java Spring Boot | Transaction processing | HTTP /actuator/health |
| Notification Service | Java Spring Boot | Notification delivery | HTTP /actuator/health |
| MFA Service | Java Spring Boot | Multi-factor auth | HTTP /actuator/health |
| Keycloak | Quay.io/keycloak | Identity management | Built-in |

### 4.2 Infrastructure Containers

| Service | Image | Purpose | Health Check |
|---------|--------|---------|--------------|
| PostgreSQL (Main) | postgres:15 | Keycloak database | pg_isready |
| PostgreSQL (User) | postgres:15 | User service database | pg_isready + connection test |
| PostgreSQL (Notification) | postgres:15 | Notification database | pg_isready |
| Redis | redis:7-alpine | Caching and sessions | redis-cli ping |
| ActiveMQ | apache/activemq-artemis | Message brokering | HTTP health endpoint |

## 5. Network Architecture

### 5.1 Container Networking
- **Internal Network**: `stockfellow-net` (bridge driver)
- **Service Discovery**: Docker DNS for inter-service communication
- **Port Mapping**: External access through mapped ports

### 5.2 Communication Patterns

```
Client → API Gateway → Microservices
         ↓
    Keycloak (Auth)
         ↓
    Redis (Cache)
         ↓
    Databases (Persistence)
         ↓
    ActiveMQ (Messaging)
```

## 6. Data Management Strategy

### 6.1 Database Architecture
- **Database per Service**: Ensures loose coupling and independent scaling
- **ACID Compliance**: PostgreSQL for transactional data
- **Caching Strategy**: Redis for frequently accessed data
- **Message Persistence**: ActiveMQ for reliable message delivery

### 6.2 Data Volumes
- **Persistent Volumes**: All databases use named volumes for data persistence
- **Configuration Volumes**: Mounted configuration files and initialization scripts
- **Log Volumes**: Centralized logging (future implementation)

## 7. Security Model

### 7.1 Authentication & Authorization
- **OAuth 2.0/OpenID Connect**: Via Keycloak
- **JWT Tokens**: For service-to-service communication
- **Role-Based Access Control (RBAC)**: Implemented through Keycloak realms

### 7.2 Network Security
- **Internal Communication**: Services communicate through private network
- **External Access**: Only API Gateway and Keycloak exposed
- **Secrets Management**: Environment variables for sensitive configuration

## 8. Deployment Strategies

### 8.1 Development Deployment
```bash
# Start entire stack
docker-compose up -d

# Start specific services
docker-compose up -d postgres redis keycloak
docker-compose up -d api-gateway user-service
```

### 8.2 Production Deployment (Kubernetes Migration Path)
```yaml
# Example Kubernetes deployment structure
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: stockfellow/user-service:latest
        ports:
        - containerPort: 4020
```

### 8.3 CI/CD Pipeline Integration
1. **Build Stage**: Docker image creation
2. **Test Stage**: Container-based testing
3. **Registry Stage**: Push to container registry
4. **Deploy Stage**: Rolling deployment to target environment

## 9. Quality Requirements Support

### 9.1 Scalability
- **Horizontal Scaling**: Microservices can be independently scaled
- **Database Scaling**: Read replicas and connection pooling
- **Cache Scaling**: Redis clustering support
- **Load Distribution**: API Gateway handles request routing

### 9.2 Reliability
- **Health Checks**: All services implement health monitoring
- **Circuit Breaker**: Fault tolerance patterns (future implementation)
- **Data Persistence**: Volume-based data protection
- **Service Recovery**: Automatic container restart policies

### 9.3 Maintainability
- **Service Independence**: Services can be updated independently
- **Configuration Management**: Environment-based configuration
- **Monitoring & Logging**: Health endpoints and structured logging
- **Version Control**: Container tagging and rollback capabilities

### 9.4 Performance
- **Caching Strategy**: Redis for improved response times
- **Connection Pooling**: Database connection optimization
- **Asynchronous Processing**: Message queues for non-blocking operations
- **Resource Optimization**: Container resource limits and requests

## 10. Monitoring and Observability

### 10.1 Health Monitoring
- **Application Health**: Spring Boot Actuator endpoints
- **Infrastructure Health**: Container and resource monitoring
- **Database Health**: Connection and performance monitoring

### 10.2 Logging Strategy
- **Structured Logging**: JSON-formatted logs
- **Log Levels**: Configurable per service
- **Centralized Logging**: Future ELK stack integration

## 11. Deployment Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    External Network                         │
│  ┌─────────┐  ┌─────────┐  ┌─────────────────────────────┐  │
│  │ Client  │  │ Client  │  │        Load Balancer        │  │
│  │   Web   │  │ Mobile  │  │       (Future)              │  │
│  └─────────┘  └─────────┘  └─────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────────┐
│                 DMZ / Edge Network                          │
│  ┌─────────────────┐         ┌─────────────────────────────┐ │
│  │   API Gateway   │◄────────┤        Keycloak             │ │
│  │   (Port 3000)   │         │      (Port 8080)            │ │
│  │                 │         │   Identity & Access Mgmt    │ │
│  └─────────────────┘         └─────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────────┐
│               Internal Service Network                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐ │
│  │User Service │ │Group Service│ │   Transaction Service   │ │
│  │(Port 4020)  │ │(Port 4040)  │ │     (Port 4080)         │ │
│  └─────────────┘ └─────────────┘ └─────────────────────────┘ │
│                                                              │
│  ┌─────────────────────────┐    ┌─────────────────────────┐  │
│  │   Notification Service  │    │      MFA Service        │  │
│  │     (Port 4050)         │    │     (Port 8087)         │  │
│  └─────────────────────────┘    └─────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────────┐
│                  Message & Cache Layer                      │
│  ┌─────────────────┐         ┌─────────────────────────────┐ │
│  │     Redis       │         │        ActiveMQ             │ │
│  │   (Port 6379)   │         │      (Port 61616)           │ │
│  │   Caching &     │         │    Message Broker           │ │
│  │   Sessions      │         │                             │ │
│  └─────────────────┘         └─────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────────┐
│                   Data Persistence Layer                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   PostgreSQL    │ │   PostgreSQL    │ │   PostgreSQL    │ │
│  │   (Keycloak)    │ │  (User Service) │ │  (Notification) │ │
│  │   Port 5432     │ │   Port 5431     │ │   Port 5440     │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
│                                                              │
│  ┌─────────────────────────┐    ┌─────────────────────────┐  │
│  │       MongoDB           │    │    Volume Storage       │  │
│  │  (Groups & MFA Data)    │    │   (Persistent Data)     │  │
│  │                         │    │                         │  │
│  └─────────────────────────┘    └─────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## 12. Migration and Scaling Path

### 12.1 Current State → Production Ready
1. **Container Registry**: Implement proper image versioning
2. **Secrets Management**: Move from environment variables to secure vaults
3. **Load Balancing**: Add NGINX or cloud load balancer
4. **Monitoring**: Implement Prometheus and Grafana

### 12.2 Kubernetes Migration
1. **Convert Docker Compose**: Transform to Kubernetes manifests
2. **Service Mesh**: Consider Istio for advanced traffic management
3. **Auto-scaling**: Implement HPA (Horizontal Pod Autoscaler)
4. **Storage**: Migrate to persistent volume claims

### 12.3 Cloud-Native Enhancements
1. **Managed Services**: Replace self-hosted databases with cloud equivalents
2. **Serverless Functions**: Consider AWS Lambda for event processing
3. **CDN Integration**: Add CloudFront/CloudFlare for static content
4. **Multi-Region**: Implement cross-region deployment for disaster recovery

## 13. Conclusion

The StockFellow deployment model provides a solid foundation for a scalable, maintainable microservices architecture. The containerized approach ensures consistency across environments while the service-oriented design supports independent scaling and development. The current Docker Compose configuration serves as an excellent stepping stone toward a full Kubernetes production deployment, with clear migration paths for enhanced scalability and reliability.