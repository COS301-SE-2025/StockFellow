# WebAuthn Service Dockerfile
# Multi-stage build for optimized production image

# Stage 1: Build stage
FROM maven:3.8.4-openjdk-11 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files first (for better caching)
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -g 1001 webauthn && \
    useradd -r -u 1001 -g webauthn webauthn

# Install necessary packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    dumb-init && \
    rm -rf /var/lib/apt/lists/*

# Copy the JAR file from build stage
COPY --from=builder /app/target/webauthn-service-*.jar /app/webauthn-service.jar

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R webauthn:webauthn /app

# Switch to non-root user
USER webauthn

# Expose the port
EXPOSE 4090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:4090/api/webauthn/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar webauthn-service.jar"]
