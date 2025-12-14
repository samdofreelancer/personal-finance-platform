package com.pocketwatch.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * User Repository
 * 
 * Provides data access operations for User entities.
 * Follows the Repository pattern from Domain-Driven Design.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Find user by Google Subject ID
     * 
     * @param googleSubject Google's unique subject ID
     * @return Optional containing User if found
     */
    Optional<User> findByGoogleSubject(String googleSubject);
    
    /**
     * Find user by email
     * 
     * @param email User's email address
     * @return Optional containing User if found
     */
    Optional<User> findByEmail(String email);
}
