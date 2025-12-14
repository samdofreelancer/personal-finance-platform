package com.pocketwatch.identity.api;

import com.pocketwatch.identity.application.AuthenticationService;
import com.pocketwatch.identity.application.UserResponse;
import com.pocketwatch.identity.infrastructure.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * 
 * Endpoints:
 * - POST /auth/google - Exchange Google ID Token for JWT
 * - GET /auth/me - Get current user information
 * 
 * CORS is handled by Spring Security configuration.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Google OAuth callback handler
     * 
     * Accepts Google ID Token from frontend and returns internal JWT.
     * 
     * Request:
     * POST /auth/google
     * Content-Type: application/json
     * { "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ..." }
     * 
     * Response:
     * { "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
     * 
     * @param request GoogleLoginRequest with ID Token
     * @return AuthResponse with internal JWT
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            if (request.getIdToken() == null || request.getIdToken().isBlank()) {
                return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("ID token is required"));
            }
            
            log.info("Processing Google login");
            
            // Authenticate user and get internal JWT
            String internalJwt = authenticationService.authenticateWithGoogle(request.getIdToken());
            
            return ResponseEntity.ok(
                AuthResponse.builder()
                    .accessToken(internalJwt)
                    .build()
            );
        } catch (AuthenticationService.AuthenticationException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Authentication failed"));
        }
    }
    
    /**
     * Get current user information
     * 
     * Requires valid JWT token in Authorization header.
     * 
     * Request:
     * GET /auth/me
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * Response:
     * {
     *   "id": "uuid",
     *   "email": "user@example.com",
     *   "name": "John Doe",
     *   "picture": "https://..."
     * }
     * 
     * @return User information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Extract JWT from SecurityContext
            String jwt = extractJwtFromContext();
            if (jwt == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("No token provided"));
            }
            
            // Validate and extract user ID from JWT
            if (!jwtTokenProvider.validateToken(jwt)) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid or expired token"));
            }
            
            String userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            // Get user information
            UserResponse user = authenticationService.getUserInfo(userId);
            
            return ResponseEntity.ok(user);
        } catch (AuthenticationService.AuthenticationException e) {
            log.warn("Failed to get user info: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting user info", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to get user information"));
        }
    }
    
    /**
     * Extract JWT from SecurityContext (Authorization header)
     * 
     * @return JWT token or null if not found
     */
    private String extractJwtFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        return null;
    }
    
    /**
     * Error response wrapper
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
    }
}
