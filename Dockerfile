# Stage 1: Build the application using Eclipse Temurin JDK 21 and Maven Wrapper
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and POM first for efficient layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies offline
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build the production executable jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal, secure production runtime with Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine

# Install curl for container health checks if needed
RUN apk add --no-cache curl

# Create dedicated non-root application user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/target/sms-*.jar app.jar

# Set file ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to unprivileged non-root user
USER appuser

EXPOSE 8081

# Healthcheck targeting Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
