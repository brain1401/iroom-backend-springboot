#!/bin/bash

# =============================================================================
# Docker Build Script for Spring Boot 3.5.4 Application
# =============================================================================
# This script provides convenient commands for building and managing Docker
# images for the Spring Boot application.
#
# Usage:
#   ./docker-build.sh [command] [options]
#
# Commands:
#   build       - Build the application and Docker image
#   build-cds   - Build CDS-enabled Docker image
#   clean       - Clean build artifacts and Docker images
#   run         - Run the application with Docker Compose
#   stop        - Stop and remove containers
#   logs        - View application logs
#   help        - Show this help message

set -e

# Configuration
APP_NAME="iroom-backend"
IMAGE_NAME="iroom/spring-backend"
VERSION="0.0.1-SNAPSHOT"
REGISTRY=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Build the Spring Boot application
build_app() {
    log_info "Building Spring Boot application..."
    ./gradlew clean bootJar
    
    if [ $? -eq 0 ]; then
        log_success "Application built successfully"
    else
        log_error "Application build failed"
        exit 1
    fi
}

# Build standard Docker image
build_docker() {
    log_info "Building Docker image: $IMAGE_NAME:$VERSION"
    
    docker build \
        --tag "$IMAGE_NAME:$VERSION" \
        --tag "$IMAGE_NAME:latest" \
        --build-arg JAR_FILE=build/libs/*.jar \
        .
    
    if [ $? -eq 0 ]; then
        log_success "Docker image built successfully"
        docker images | grep "$IMAGE_NAME"
    else
        log_error "Docker image build failed"
        exit 1
    fi
}

# Build CDS-enabled Docker image
build_docker_cds() {
    log_info "Building CDS-enabled Docker image: $IMAGE_NAME:$VERSION-cds"
    
    docker build \
        --file Dockerfile.cds \
        --tag "$IMAGE_NAME:$VERSION-cds" \
        --tag "$IMAGE_NAME:latest-cds" \
        --build-arg JAR_FILE=build/libs/*.jar \
        .
    
    if [ $? -eq 0 ]; then
        log_success "CDS-enabled Docker image built successfully"
        docker images | grep "$IMAGE_NAME"
    else
        log_error "CDS-enabled Docker image build failed"
        exit 1
    fi
}

# Clean build artifacts and Docker images
clean() {
    log_info "Cleaning build artifacts and Docker images..."
    
    # Clean Gradle build
    ./gradlew clean
    
    # Remove Docker images
    docker rmi "$IMAGE_NAME:$VERSION" 2>/dev/null || true
    docker rmi "$IMAGE_NAME:latest" 2>/dev/null || true
    docker rmi "$IMAGE_NAME:$VERSION-cds" 2>/dev/null || true
    docker rmi "$IMAGE_NAME:latest-cds" 2>/dev/null || true
    
    # Remove dangling images
    docker image prune -f
    
    log_success "Cleanup completed"
}

# Run with Docker Compose
run_compose() {
    log_info "Starting application with Docker Compose..."
    docker-compose up --build -d
    
    if [ $? -eq 0 ]; then
        log_success "Application started successfully"
        log_info "Application URL: http://localhost:3055"
        log_info "Swagger UI: http://localhost:3055/api/swagger-ui.html"
        log_info "Adminer: http://localhost:8080"
    else
        log_error "Failed to start application"
        exit 1
    fi
}

# Stop Docker Compose
stop_compose() {
    log_info "Stopping Docker Compose services..."
    docker-compose down
    log_success "Services stopped"
}

# View logs
view_logs() {
    log_info "Viewing application logs..."
    docker-compose logs -f app
}

# Push to registry
push_image() {
    if [ -z "$REGISTRY" ]; then
        log_warning "No registry configured. Skipping push."
        return
    fi
    
    log_info "Pushing image to registry: $REGISTRY"
    
    # Tag for registry
    docker tag "$IMAGE_NAME:$VERSION" "$REGISTRY/$IMAGE_NAME:$VERSION"
    docker tag "$IMAGE_NAME:latest" "$REGISTRY/$IMAGE_NAME:latest"
    
    # Push to registry
    docker push "$REGISTRY/$IMAGE_NAME:$VERSION"
    docker push "$REGISTRY/$IMAGE_NAME:latest"
    
    log_success "Image pushed to registry"
}

# Show help
show_help() {
    cat << EOF
Docker Build Script for Spring Boot 3.5.4 Application

Usage: $0 [command] [options]

Commands:
    build       Build the application and standard Docker image
    build-cds   Build CDS-enabled Docker image
    clean       Clean build artifacts and Docker images
    run         Run the application with Docker Compose
    stop        Stop and remove Docker Compose containers
    logs        View application logs
    push        Push images to registry (requires REGISTRY env var)
    help        Show this help message

Examples:
    $0 build                    # Build standard image
    $0 build-cds               # Build CDS-enabled image
    $0 run                     # Start with Docker Compose
    $0 logs                    # View logs
    $0 clean                   # Clean everything

Environment Variables:
    REGISTRY                   # Docker registry URL for push command

EOF
}

# Main script logic
main() {
    check_docker
    
    case "${1:-help}" in
        build)
            build_app
            build_docker
            ;;
        build-cds)
            build_app
            build_docker_cds
            ;;
        clean)
            clean
            ;;
        run)
            run_compose
            ;;
        stop)
            stop_compose
            ;;
        logs)
            view_logs
            ;;
        push)
            push_image
            ;;
        help|*)
            show_help
            ;;
    esac
}

# Run main function with all arguments
main "$@"