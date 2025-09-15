FROM openjdk:17-jdk-slim

VOLUME /tmp

# Add application jar
COPY target/admin-service-1.0.0.jar app.jar

# Set JVM options for debugging
ENV JVM_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5010"

# Run the application
ENTRYPOINT exec java $JVM_OPTS -jar /app.jar