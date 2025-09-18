# Multi-stage build for user-service - Production Optimized

# Build stage
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Install Maven in Alpine
RUN apk add --no-cache maven

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B -q

# Copy source code
COPY src ./src

# Build the application with production profile and optimizations
RUN mvn clean package -DskipTests -Pprod -Dmaven.javadoc.skip=true -q

# Production runtime stage
FROM amazoncorretto:17-alpine

WORKDIR /app

# Install curl for health checks (Alpine way)
RUN apk add --no-cache curl

# Copy the built JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security (Alpine way)
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 4020

# Production health check (optimized for user service - frequent access pattern)
HEALTHCHECK --interval=45s --timeout=5s --start-period=75s --retries=3 \
  CMD curl -f http://localhost:4020/actuator/health || exit 1

# Simplified production JVM settings (avoiding JVM startup issues)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dspring.jmx.enabled=false"

# Production entrypoint optimized for user management workloads
CMD ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar app.jar"]