# Multi-stage build for transaction-service - Production Optimized

# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

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
FROM eclipse-temurin:17-jre-alpine

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
EXPOSE 4080

# Production health check (optimized for transaction service)
HEALTHCHECK --interval=45s --timeout=8s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:4080/actuator/health || exit 1

# Production-optimized JVM settings for transaction service
# Transaction services need careful memory management for data consistency
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=100 \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -XX:+TieredCompilation \
               -XX:TieredStopAtLevel=4 \
               -Xss256k \
               -XX:+UseCompressedOops \
               -XX:+UseCompressedClassPointers \
               -Djava.awt.headless=true \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod \
               -Dspring.jmx.enabled=false \
               -Dmanagement.endpoints.jmx.exposure.exclude=* \
               -Dspring.transaction.default-timeout=30 \
               -Dlogging.level.org.springframework.transaction=INFO"

# Production entrypoint optimized for transaction processing
CMD ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar app.jar"]