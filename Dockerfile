# ==========================================
# SMART SPEND - PRODUCTION DOCKERFILE
# Multi-Stage Build for Render.com / Railway
# ==========================================

# Stage 1: Build the Application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy poms
COPY pom.xml .
COPY smart-spend-backend/pom.xml smart-spend-backend/
COPY smart-spend-frontend/pom.xml smart-spend-frontend/

# Download dependencies
RUN mvn dependency:go-offline -B -pl smart-spend-backend -am

# Copy backend source and static web assets
COPY smart-spend-backend/src smart-spend-backend/src

# Package executable jar
RUN mvn clean package -pl smart-spend-backend -am -DskipTests

# Stage 2: Runtime Container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar
COPY --from=builder /workspace/smart-spend-backend/target/smart-spend-backend-1.0.0.jar app.jar

# Render dynamic port configuration and memory tuning for free 512MB tier
EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS="-Xmx360m -Xms128m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
