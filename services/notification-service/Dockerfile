# Use Maven official image as builder
FROM maven:3-openjdk-17-slim AS builder

WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Use lightweight JDK image to run the app
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the builder image
COPY --from=builder /app/target/*.jar app.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
