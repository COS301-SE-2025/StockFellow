# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install necessary packages for debugging and health checks
RUN apt-get update && apt-get install -y \
    curl \
    netcat-traditional \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper and pom.xml first for better caching
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create logs directory
RUN mkdir -p /app/logs

# Copy the built jar file
RUN cp target/*.jar app.jar

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring

# Expose the port
EXPOSE 4050

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# Environment variables for external services
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/notification_db
ENV SPRING_DATASOURCE_USERNAME=admin
ENV SPRING_DATASOURCE_PASSWORD=admin123
ENV ACTIVEMQ_BROKER_URL=tcp://activemq:61616
ENV ACTIVEMQ_USER=admin
ENV ACTIVEMQ_PASSWORD=admin

# JVM options for better container performance
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Wait script for dependencies (inline)
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# Function to wait for service\n\
wait_for_service() {\n\
    local host=$1\n\
    local port=$2\n\
    local service_name=$3\n\
    \n\
    echo "Waiting for $service_name at $host:$port..."\n\
    while ! nc -z "$host" "$port"; do\n\
        echo "$service_name is unavailable - sleeping"\n\
        sleep 2\n\
    done\n\
    echo "$service_name is up!"\n\
}\n\
\n\
# Extract host and port from environment variables\n\
POSTGRES_HOST=$(echo $SPRING_DATASOURCE_URL | sed "s|jdbc:postgresql://||" | cut -d":" -f1)\n\
POSTGRES_PORT=$(echo $SPRING_DATASOURCE_URL | sed "s|jdbc:postgresql://||" | cut -d":" -f2 | cut -d"/" -f1)\n\
ACTIVEMQ_HOST=$(echo $ACTIVEMQ_BROKER_URL | sed "s|tcp://||" | cut -d":" -f1)\n\
ACTIVEMQ_PORT=$(echo $ACTIVEMQ_BROKER_URL | sed "s|tcp://||" | cut -d":" -f2)\n\
\n\
# Wait for PostgreSQL\n\
wait_for_service "$POSTGRES_HOST" "$POSTGRES_PORT" "PostgreSQL"\n\
\n\
# Wait for ActiveMQ\n\
wait_for_service "$ACTIVEMQ_HOST" "$ACTIVEMQ_PORT" "ActiveMQ"\n\
\n\
echo "All dependencies are ready!"\n\
\n\
# Start the application\n\
exec java $JAVA_OPTS -jar app.jar "$@"' > /app/wait-and-start.sh

RUN chmod +x /app/wait-and-start.sh

# Use the wait script as entrypoint
ENTRYPOINT ["/app/wait-and-start.sh"]