# Multi-stage build for group-service - Production Optimized with Alpine

# Build stage - Use Eclipse Temurin for better support and smaller size
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

# Production runtime stage - Minimal Alpine JRE
FROM amazoncorretto:17-alpine

# Install curl for health checks (minimal Alpine package)
RUN apk add --no-cache curl

WORKDIR /app

# Copy the built JAR from build stage with specific naming
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user (Alpine way)
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose only the application port
EXPOSE 4040

# Optimized health check for production (longer intervals to save resources)
HEALTHCHECK --interval=60s --timeout=5s --start-period=90s --retries=2 \
  CMD curl -f http://localhost:4040/actuator/health || exit 1

# Production-optimized JVM settings for resource-constrained environments
ENV JAVA_OPTS="-server \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=4 \
  -Xss256k \
  -Djava.awt.headless=true \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dspring.jmx.enabled=false \
  -Dmanagement.endpoints.jmx.exposure.exclude=*"

# Production entrypoint with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar app.jar"]