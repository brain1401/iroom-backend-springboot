# =============================================================================
# Spring Boot 3.5.4 Optimized Multi-stage Dockerfile
# =============================================================================
# Based on Spring Boot Official Documentation:
# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles/
#
# Features:
# - Multi-stage build for optimized image size
# - Layer extraction using jarmode=tools for better caching
# - Java 21 support with Bellsoft Liberica JRE
# - Security optimizations with non-root user
# - Health check integration
# - Memory and performance optimizations
# =============================================================================

# -----------------------------------------------------------------------------
# Builder Stage: Extract application layers
# -----------------------------------------------------------------------------
FROM bellsoft/liberica-openjre-debian:21-cds AS builder

# Set working directory in builder
WORKDIR /builder

# Define build argument for JAR file location (Gradle build)
ARG JAR_FILE=build/libs/*.jar

# Copy the built JAR file to builder container
COPY ${JAR_FILE} application.jar

# Extract JAR file using Spring Boot's efficient layering
# This creates separate directories for each layer type:
# - dependencies/ (third-party libraries)
# - spring-boot-loader/ (Spring Boot loader classes)
# - snapshot-dependencies/ (snapshot dependencies)
# - application/ (application code)
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# -----------------------------------------------------------------------------
# Runtime Stage: Create optimized runtime image
# -----------------------------------------------------------------------------
FROM bellsoft/liberica-openjre-debian:21-cds

# Metadata
LABEL maintainer="iroom-backend-team"
LABEL description="Spring Boot 3.5.4 Backend Application"
LABEL version="0.0.1-SNAPSHOT"

# Create application directory
WORKDIR /application

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy extracted layers from builder stage
# Each COPY instruction creates a separate Docker layer
# This enables Docker to cache layers and only pull changes when needed
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Change ownership to spring user
RUN chown -R spring:spring /application

# Switch to non-root user
USER spring

# Expose application port
EXPOSE 3055

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:3055/api/system/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Start the application
# This jar only contains application code and references to extracted dependencies
# This layout is efficient for startup and layer caching
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar application.jar"]