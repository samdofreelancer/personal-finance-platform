package com.pocketwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Personal Finance Platform - Backend Application
 * 
 * Main entry point for the Spring Boot application.
 * 
 * Features:
 * - Google OIDC authentication
 * - JWT token management
 * - RESTful API endpoints
 * - Database persistence (H2 for dev, upgrade to PostgreSQL for production)
 */
@SpringBootApplication
public class PersonalFinanceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceApplication.class, args);
    }
}
