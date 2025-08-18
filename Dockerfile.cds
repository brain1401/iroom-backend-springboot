# =============================================================================
# Spring Boot 3.5.4 CDS-Enabled Dockerfile
# =============================================================================
# Based on Spring Boot Official Documentation:
# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles/
#
# Features:
# - Class Data Sharing (CDS) for faster startup times
# - Multi-stage build with CDS archive generation
# - Layer extraction using jarmode=tools
# - Java 21 with Bellsoft Liberica JRE CDS support
# - Security optimizations with non-root user
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
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# -----------------------------------------------------------------------------
# Runtime Stage: Create CDS-optimized runtime image
# -----------------------------------------------------------------------------
FROM bellsoft/liberica-openjre-debian:21-cds

# Metadata
LABEL maintainer="iroom-backend-team"
LABEL description="Spring Boot 3.5.4 Backend Application with CDS"
LABEL version="0.0.1-SNAPSHOT"

# Create application directory
WORKDIR /application

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy extracted layers from builder stage
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Generate CDS archive by running a training session
# This creates application.jsa file with optimized class data
RUN java -XX:ArchiveClassesAtExit=application.jsa \
         -Dspring.context.exit=onRefresh \
         -jar application.jar

# Change ownership to spring user
RUN chown -R spring:spring /application

# Switch to non-root user
USER spring

# Expose application port
EXPOSE 3055

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
    CMD curl -f http://localhost:3055/api/system/health || exit 1

# Set JVM options for production with CDS
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Start the application with CDS enabled
# SharedArchiveFile enables the use of the generated CDS archive for faster startup
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -XX:SharedArchiveFile=application.jsa -jar application.jar"]