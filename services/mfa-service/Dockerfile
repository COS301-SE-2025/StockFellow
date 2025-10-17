# Multi-stage build for mfa-service
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Install Maven in Alpine
RUN apk add --no-cache maven

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:17-alpine

WORKDIR /app

# Install curl for health checks (Alpine way)
RUN apk add --no-cache curl

# Create a non-root user for security (Alpine way)
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Copy the built JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8087

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]