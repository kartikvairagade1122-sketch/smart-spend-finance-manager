# ==========================================
# SMART SPEND - PRODUCTION DOCKERFILE
# Multi-Stage Build for Render.com / Railway
# ==========================================

# Stage 1: Build the Application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy root pom and module poms for dependency caching
COPY pom.xml .
COPY smart-spend-backend/pom.xml smart-spend-backend/
COPY smart-spend-frontend/pom.xml smart-spend-frontend/

# Download dependencies
RUN mvn dependency:go-offline -B -pl smart-spend-backend -am

# Copy backend source and static web assets
COPY smart-spend-backend/src smart-spend-backend/src

# Package the Spring Boot executable jar with embedded web app
RUN mvn clean package -pl smart-spend-backend -am -DskipTests

# Stage 2: Runtime Container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S smartspend && adduser -S smartspend -G smartspend
USER smartspend:smartspend

# Copy built jar from builder stage
COPY --from=builder /workspace/smart-spend-backend/target/smart-spend-backend-1.0.0.jar app.jar

# Render injects $PORT dynamically (defaults to 8080)
EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
