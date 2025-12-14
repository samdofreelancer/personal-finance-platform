package com.pocketwatch.identity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User Domain Entity
 * 
 * Represents a user in the system. Users are identified by their Google Subject ID.
 * Email is extracted from Google ID Token and stored here.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_google_subject", columnList = "google_subject", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Google Subject ID (sub claim from Google ID Token)
     * This is unique per user per Google account
     * CRITICAL: Used as the authoritative user identifier
     */
    @Column(name = "google_subject", nullable = false, unique = true)
    private String googleSubject;
    
    /**
     * Email address extracted from Google ID Token
     */
    @Column(nullable = false, unique = true)
    private String email;
    
    /**
     * User's name from Google profile
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Picture URL from Google profile (optional)
     */
    @Column
    private String picture;
    
    /**
     * When the user was first created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * When the user was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Track when the user was last authenticated
     */
    @Column
    private LocalDateTime lastAuthenticatedAt;
    
    /**
     * Lifecycle hook - set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Lifecycle hook - update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
