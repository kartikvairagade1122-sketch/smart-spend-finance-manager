# Multi-stage Dockerfile for Smart Spend Backend (Spring Boot 3)
# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom files and dependencies first to leverage Docker layer caching
COPY pom.xml ./
COPY smart-spend-backend/pom.xml smart-spend-backend/
COPY smart-spend-frontend/pom.xml smart-spend-frontend/

# Download dependencies (offline mode preparation)
RUN mvn -B dependency:go-offline -pl smart-spend-backend -am

# Copy backend source code
COPY smart-spend-backend/src smart-spend-backend/src

# Package backend skipping tests (tests executed in CI)
RUN mvn clean package -pl smart-spend-backend -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root group and user for enhanced security
RUN groupadd -r smartspend && useradd -r -g smartspend smartspend

# Copy the built jar from builder stage
COPY --from=builder /app/smart-spend-backend/target/smart-spend-backend-1.0.0.jar app.jar

# Set ownership to non-root user
RUN chown -R smartspend:smartspend /app
USER smartspend

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=smartspend \
    DB_USER=smartspend_user \
    DB_PASSWORD=smartspend_secure_pass

# Expose Spring Boot REST API port
EXPOSE 8080

# Healthcheck to ensure container is healthy
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
