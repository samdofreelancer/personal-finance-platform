package com.pocketwatch.identity.application;

import com.pocketwatch.identity.domain.User;
import com.pocketwatch.identity.domain.UserRepository;
import com.pocketwatch.identity.infrastructure.GoogleTokenVerifier;
import com.pocketwatch.identity.infrastructure.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Authentication Application Service
 * 
 * Orchestrates the Google OIDC authentication flow:
 * 1. Verify Google ID Token signature
 * 2. Extract user claims (email, Google subject)
 * 3. Find or create user in database
 * 4. Issue internal JWT token
 * 
 * Separation of concerns:
 * - Infrastructure layer handles Google verification and token generation
 * - Domain layer (User entity) manages user data
 * - Application service orchestrates the flow
 * 
 * CRITICAL: This is the trust boundary. After this point, we trust only our own JWT.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthenticationService {
    
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    /**
     * Authenticate user with Google ID Token
     * 
     * Flow:
     * 1. Verify Google ID Token signature (CRITICAL)
     * 2. Extract user claims from verified token
     * 3. Find or create user in database
     * 4. Generate internal JWT
     * 
     * @param googleIdToken Google ID Token from frontend
     * @return Internal JWT token
     * @throws AuthenticationException if token verification fails
     */
    public String authenticateWithGoogle(String googleIdToken) 
            throws AuthenticationException {
        
        try {
            // Step 1: Verify Google ID Token signature
            // This is CRITICAL - we must verify the token before trusting any claims
            Claims claims = googleTokenVerifier.verifyGoogleToken(googleIdToken);
            
            // Step 2: Extract user information from verified token
            String googleSubject = (String) claims.get("sub");
            String email = (String) claims.get("email");
            String name = (String) claims.getOrDefault("name", email);
            String picture = (String) claims.get("picture");
            
            if (googleSubject == null || email == null) {
                throw new AuthenticationException(
                    "Invalid Google token: missing required claims (sub, email)"
                );
            }
            
            log.info("Google token verified for user: {}", email);
            
            // Step 3: Find or create user
            User user = userRepository.findByGoogleSubject(googleSubject)
                .orElseGet(() -> createNewUser(googleSubject, email, name, picture));
            
            // Update last authenticated timestamp
            user.setLastAuthenticatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("User authenticated successfully: {} ({})", user.getId(), email);
            
            // Step 4: Issue internal JWT
            String internalJwt = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
            
            return internalJwt;
            
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new AuthenticationException("Google token verification failed", e);
        }
    }
    
    /**
     * Create new user from Google claims
     * 
     * @param googleSubject Google's unique subject ID
     * @param email User's email
     * @param name User's name
     * @param picture User's profile picture URL
     * @return Newly created User
     */
    private User createNewUser(String googleSubject, String email, String name, String picture) {
        User user = User.builder()
            .googleSubject(googleSubject)
            .email(email)
            .name(name)
            .picture(picture)
            .build();
        
        user = userRepository.save(user);
        log.info("Created new user: {} ({})", user.getId(), email);
        return user;
    }
    
    /**
     * Get user information from database by ID
     * 
     * @param userId User ID
     * @return User information
     * @throws AuthenticationException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String userId) throws AuthenticationException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .picture(user.getPicture())
            .build();
    }
    
    /**
     * Custom authentication exception
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
