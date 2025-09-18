# Multi-stage build for notification-service - Production Optimized

# Build stage
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Install Maven in Alpine
RUN apk add --no-cache maven

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source and build with production optimizations
COPY src ./src
RUN mvn clean package -DskipTests -Pprod -Dmaven.javadoc.skip=true -q

# Production runtime stage
FROM amazoncorretto:17-alpine

WORKDIR /app

# Install curl for health checks (Alpine way, not apt-get)
RUN apk add --no-cache curl

# Copy the built JAR from builder stage
COPY --from=builder /app/target/notification-service-*.jar app.jar

# Create non-root user for security (Alpine way)
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose only the application port (remove debug port for production)
EXPOSE 4050

# Production health check (optimized intervals)
HEALTHCHECK --interval=60s --timeout=5s --start-period=90s --retries=2 \
  CMD curl -f http://localhost:4050/actuator/health || exit 1

# Production-optimized JVM settings for notification service
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

# Production entrypoint with JVM optimizations
CMD ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar app.jar"]