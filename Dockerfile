# --- Stage 1: Build the application using a JDK 8 environment ---
FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application JAR
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Create the final, lightweight image using a JRE 8 environment ---
FROM openjdk:8-jre-slim
WORKDIR /app

# Copy the built JAR from the 'build' stage (using the correct artifactId)
COPY --from=builder /app/target/currency-api-demo-*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]