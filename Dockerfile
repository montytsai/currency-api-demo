# --- Stage 1: Build Environment ---
FROM maven:3.9.6-eclipse-temurin-17-focal AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application JAR
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Create the final, lightweight image ---
FROM openjdk:8-jre-slim
WORKDIR /app

# Copy the built JAR from the 'build' stage
COPY --from=builder /app/target/auth-kit-*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Expose the port the application runs on
ENTRYPOINT ["java", "-jar", "app.jar"]