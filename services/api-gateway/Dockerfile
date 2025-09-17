# Multi-stage build for Java Spring Boot application - Optimized for resource-constrained environments

# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11-slim AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better layer caching)
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with optimizations
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true

# Stage 2: Runtime image - Use Eclipse Temurin (official OpenJDK builds)
FROM eclipse-temurin:11-jre-alpine

# Install curl for health checks using alpine package manager
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/api-gateway-*.jar app.jar

# Create a non-root user for security (Alpine way)
RUN addgroup -S appuser && adduser -S appuser -G appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose only the application port (remove debug port for production)
EXPOSE 3000

# Optimized health check with longer intervals for resource savings
HEALTHCHECK --interval=60s --timeout=5s --start-period=90s --retries=2 \
  CMD curl -f http://localhost:3000/actuator/health || exit 1

# Optimized JVM options for resource-constrained environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=70.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.jmx.enabled=false \
               -Dmanagement.endpoints.jmx.exposure.exclude=*"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS $JVM_OPTS -jar app.jar"]