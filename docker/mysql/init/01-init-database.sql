-- =============================================================================
-- Database Initialization Script for iRoom Backend
-- =============================================================================
-- This script creates the initial database structure and sample data
-- for the Spring Boot application.

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS iroom_backend_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE iroom_backend_db;

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON iroom_backend_db.* TO 'iroom_user'@'%';
FLUSH PRIVILEGES;

-- Create sample tables (these will be managed by JPA/Hibernate)
-- This is just for reference - actual tables will be created by Spring Boot

-- Users table structure (reference only)
/*
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
*/

-- Insert sample data (if needed)
-- This will be handled by DataLoader or test data in the application

-- Performance optimization indexes
-- (These will be created by JPA @Index annotations)

-- Log initialization completion
SELECT 'Database initialization completed' AS status;